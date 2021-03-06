/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.search.internal;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.FacetRangeTO;
import org.craftercms.studio.api.v1.to.FacetTO;
import org.craftercms.studio.api.v2.service.search.internal.SearchServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.search.PermissionAwareSearchService;
import org.craftercms.studio.model.search.SearchFacet;
import org.craftercms.studio.model.search.SearchFacetRange;
import org.craftercms.studio.model.search.SearchResultItem;
import org.craftercms.studio.model.search.SearchParams;
import org.craftercms.studio.model.search.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Required;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;

/**
 * Default implementation of {@link SearchServiceInternal}
 * @author joseross
 */
public class SearchServiceInternalImpl implements SearchServiceInternal {

    public static final String CONFIG_KEY_FIELDS = "studio.search.fields.search";
    public static final String CONFIG_KEY_FACETS = "studio.search.facets";
    public static final String CONFIG_KEY_TYPES = "studio.search.types";

    public static final String CONFIG_KEY_NAME = "name";
    public static final String CONFIG_KEY_FIELD = "field";

    public static final String CONFIG_KEY_FACET_DATE = "date";
    public static final String CONFIG_KEY_FACET_MULTIPLE = "multiple";
    public static final String CONFIG_KEY_FACET_RANGES = "ranges";
    public static final String CONFIG_KEY_FACET_RANGE_LABEL = "label";
    public static final String CONFIG_KEY_FACET_RANGE_FROM = "from";
    public static final String CONFIG_KEY_FACET_RANGE_TO = "to";

    public static final String CONFIG_KEY_TYPE_MATCHES = "matches";

    public static final String CONFIG_KEY_FIELD_BOOST = "boost";

    public static final String FACET_RANGE_MIN = "min";
    public static final String FACET_RANGE_MAX = "max";

    public static final String DEFAULT_MIME_TYPE = "application/xml";

    public static final Pattern EXACT_MATCH_PATTERN = Pattern.compile(".*(\"([^\"]+)\").*");

    /**
     * Name of the field for paths
     */
    protected String pathFieldName;

    /**
     * Name of the field for internal name
     */
    protected String internalNameFieldName;

    /**
     * Name of the field for last edit date
     */
    protected String lastEditFieldName;

    /**
     * Name of the field for last edit user
     */
    protected String lastEditorFieldName;

    /**
     * Name of the field for size
     */
    protected String sizeFieldName;

    /**
     * Name of the field for mimeType
     */
    protected String mimeTypeName;

    /**
     * List of fields to include during searching
     */
    protected Map<String, Float> searchFields;

    /**
     * List of fields to include during highlighting
     */
    protected String[] highlightFields;

    /**
     * Number of characters to include for snippets
     */
    protected int snippetSize;

    /**
     * Number of snippets to generate for each file
     */
    protected int numberOfSnippets;

    /**
     * Default label used for unknown file types
     */
    protected String defaultType;

    /**
     * The Elasticsearch service
     */
    protected PermissionAwareSearchService elasticsearchService;

    /**
     * The Studio configuration
     */
    protected StudioConfiguration studioConfiguration;

    /**
     * The site configuration
     */
    protected ServicesConfig servicesConfig;

    /**
     * Configurations for facets
     */
    protected Map<String, FacetTO> facets;

    /**
     * Configurations for types
     */
    protected Map<String, HierarchicalConfiguration<ImmutableNode>> types;

    @Required
    public void setPathFieldName(final String pathFieldName) {
        this.pathFieldName = pathFieldName;
    }

    @Required
    public void setInternalNameFieldName(final String internalNameFieldName) {
        this.internalNameFieldName = internalNameFieldName;
    }

    @Required
    public void setLastEditFieldName(final String lastEditFieldName) {
        this.lastEditFieldName = lastEditFieldName;
    }

    @Required
    public void setLastEditorFieldName(final String lastEditorFieldName) {
        this.lastEditorFieldName = lastEditorFieldName;
    }

    @Required
    public void setSizeFieldName(final String sizeFieldName) {
        this.sizeFieldName = sizeFieldName;
    }

    @Required
    public void setMimeTypeName(final String mimeTypeName) {
        this.mimeTypeName = mimeTypeName;
    }

    @Required
    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    @Required
    public void setHighlightFields(final String[] highlightFields) {
        this.highlightFields = highlightFields;
    }

    @Required
    public void setSnippetSize(final int snippetSize) {
        this.snippetSize = snippetSize;
    }

    @Required
    public void setNumberOfSnippets(final int numberOfSnippets) {
        this.numberOfSnippets = numberOfSnippets;
    }

    @Required
    public void setElasticsearchService(final PermissionAwareSearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    /**
     * Loads facets and type mapping from the global configuration
     */
    public void init() {
        loadFieldsFromGlobalConfiguration();
        loadTypesFromGlobalConfiguration();
        loadFacetsFromGlobalConfiguration();
    }

    protected void loadFieldsFromGlobalConfiguration() {
        searchFields = new TreeMap<>();

        List<HierarchicalConfiguration<ImmutableNode>> fieldsConfig =
                studioConfiguration.getSubConfigs(CONFIG_KEY_FIELDS);

        if(CollectionUtils.isNotEmpty(fieldsConfig)) {
            fieldsConfig.forEach(fieldConfig ->
                searchFields.put(fieldConfig.getString(CONFIG_KEY_NAME), fieldConfig.getFloat(CONFIG_KEY_FIELD_BOOST))
            );
        }
    }

    /**
     * Loads the facets from the global configuration
     */
    protected void loadFacetsFromGlobalConfiguration() {
        facets = new HashMap<>();

        List<HierarchicalConfiguration<ImmutableNode>> facetsConfig =
            studioConfiguration.getSubConfigs(CONFIG_KEY_FACETS);

        if(CollectionUtils.isNotEmpty(facetsConfig)) {
            facetsConfig.forEach(facetConfig -> {
                FacetTO facet = new FacetTO();
                facet.setName(facetConfig.getString(CONFIG_KEY_NAME));
                facet.setField(facetConfig.getString(CONFIG_KEY_FIELD));
                facet.setDate(facetConfig.getBoolean(CONFIG_KEY_FACET_DATE, false));
                facet.setMultiple(facetConfig.getBoolean(CONFIG_KEY_FACET_MULTIPLE, false));

                List<HierarchicalConfiguration<ImmutableNode>> ranges =
                    facetConfig.configurationsAt(CONFIG_KEY_FACET_RANGES);

                if(CollectionUtils.isNotEmpty(ranges)) {
                    facet.setRanges(
                        ranges.stream().map(rangeConfig -> {
                            FacetRangeTO range = new FacetRangeTO();
                            range.setLabel(rangeConfig.getString(CONFIG_KEY_FACET_RANGE_LABEL));
                            if(rangeConfig.containsKey(CONFIG_KEY_FACET_RANGE_FROM) &&
                                rangeConfig.containsKey(CONFIG_KEY_FACET_RANGE_TO)) {
                                range.setFrom(rangeConfig.getString(CONFIG_KEY_FACET_RANGE_FROM));
                                range.setTo(rangeConfig.getString(CONFIG_KEY_FACET_RANGE_TO));
                            } else if(rangeConfig.containsKey(CONFIG_KEY_FACET_RANGE_FROM)) {
                                range.setFrom(rangeConfig.getString(CONFIG_KEY_FACET_RANGE_FROM));
                            } else {
                                range.setTo(rangeConfig.getString(CONFIG_KEY_FACET_RANGE_TO));
                            }
                            return range;
                        })
                        .collect(Collectors.toList())
                    );
                }
                facets.put(facet.getName(), facet);
            });
        }
    }

    /**
     * Loads the type mapping from the global configuration
     */
    protected void loadTypesFromGlobalConfiguration() {
        types = new LinkedHashMap<>();

        List<HierarchicalConfiguration<ImmutableNode>> typesConfig =
            studioConfiguration.getSubConfigs(CONFIG_KEY_TYPES);

        typesConfig.forEach(type -> types.put(type.getString(CONFIG_KEY_NAME), type));

    }

    /**
     * Maps the information from Elasticsearch for a single {@link SearchResultItem}
     * @param source the fields returned by Elasticsearch
     * @param highlights the highlights returned by Elasticsearch
     * @return the search item object
     */
    //TODO: Implement previewUrl for supported types
    protected SearchResultItem processSearchHit(Map<String, Object> source, Map<String, HighlightField> highlights) {
        SearchResultItem item = new SearchResultItem();
        item.setPath((String) source.get(pathFieldName));
        item.setName((String) source.get(internalNameFieldName));
        item.setLastModified(Instant.parse((String) source.get(lastEditFieldName)));
        item.setLastModifier(source.get(lastEditorFieldName).toString());
        item.setSize(Long.parseLong(source.get(sizeFieldName).toString()));
        item.setType(getItemType(source));
        item.setMimeType(getMimeType(source));
        item.setSnippets(getItemSnippets(highlights));
        return item;
    }

    /**
     * Adds the required filters based on the given parameters
     * @param query the query to update
     * @param params the parameters to add
     * @param siteFacets the facets configured for the site
     */
    @SuppressWarnings("unchecked")
    protected void updateFilters(BoolQueryBuilder query, SearchParams params, Map<String, FacetTO> siteFacets) {
        var builder = params.isOrOperator()? boolQuery() : query;
        params.getFilters().forEach((filter, value) -> {
            FacetTO facetConfig;
            if(MapUtils.isNotEmpty(siteFacets)) {
                facetConfig = siteFacets.getOrDefault(filter, facets.get(filter));
            } else {
                facetConfig = facets.get(filter);
            }
            if(Objects.nonNull(facetConfig)) {
                String fieldName = facetConfig.getField();
                if(facetConfig.isRange()) {
                    RangeQueryBuilder rangeQuery = rangeQuery(fieldName);
                    Map<String, Object> range = (Map<String, Object>) value;
                    rangeQuery
                        .gte(range.get(FACET_RANGE_MIN))
                        .lt(range.get(FACET_RANGE_MAX));

                    if (params.isOrOperator()) {
                        builder.should(rangeQuery);
                    } else {
                        builder.filter(rangeQuery);
                    }
                } else if (facetConfig.isMultiple() && value instanceof List) {
                    List<Object> values = (List<Object>) value;
                    BoolQueryBuilder orQuery = boolQuery();
                    values.forEach(val -> orQuery.should(matchQuery(fieldName, val)));
                    var qb = boolQuery().must(orQuery);
                    if (params.isOrOperator()) {
                        builder.should(qb);
                    } else {
                        builder.filter(qb);
                    }
                } else {
                    var qb = matchQuery(fieldName, value);
                    if (params.isOrOperator()) {
                        builder.should(qb);
                    } else {
                        builder.filter(qb);
                    }
                }
            }
        });
        if (params.isOrOperator()) {
            query.filter(boolQuery().must(builder));
        }
    }

    /**
     * Adds the configured highlighting to the given builder
     * @param builder the search builder to update
     */
    protected void updateHighlighting(SearchSourceBuilder builder) {
        HighlightBuilder highlight = SearchSourceBuilder.highlight();
        for (String field : highlightFields) {
            highlight.field(field, snippetSize, numberOfSnippets);
        }
        builder.highlighter(highlight);
    }

    /**
     * Maps the Elasticsearch {@link SearchResponse} to a {@link SearchResult} object
     * @param response the response to map
     * @return the search result object
     */
    protected SearchResult processResults(SearchResponse response, Map<String, FacetTO> siteFacets) {
        SearchResult result = new SearchResult();
        result.setTotal(response.getHits().getTotalHits().value);

        List<SearchResultItem> items = Stream.of(response.getHits().getHits())
            .map(hit -> processSearchHit(hit.getSourceAsMap(), hit.getHighlightFields()))
            .collect(Collectors.toList());

        result.setItems(items);

        result.setFacets(processAggregations(response, siteFacets));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResult search(final String siteId, final List<String> allowedPaths, final SearchParams params)
            throws ServiceLayerException {

        Map<String, FacetTO> siteFacets = servicesConfig.getFacets(siteId);
        BoolQueryBuilder query = boolQuery();
        Map<String, Float> boostedFields = new TreeMap<>(searchFields);
        boostedFields.putAll(servicesConfig.getSearchFields(siteId));

        // A Lucene query, this was added to support the custom query for content monitoring, could be replaced later
        if(StringUtils.isNotEmpty(params.getQuery())) {
            query.must(QueryBuilders.queryStringQuery(params.getQuery()));
        }

        // Do not replace special characters, this will allow ES to handle them per field
        String rawKeywords = params.getKeywords();

        if (StringUtils.isNotEmpty(rawKeywords)) {
            // Check if the user requests an exact match with quotes
            Matcher matcher = EXACT_MATCH_PATTERN.matcher(rawKeywords);
            if (matcher.matches()) {
                // A match query without synonyms and no fuzziness is as close as we can get to an exact match
                query.must(multiMatchQuery(matcher.group(2))
                            .autoGenerateSynonymsPhraseQuery(false)
                            .type(MatchQuery.Type.PHRASE)
                            .fuzzyTranspositions(false)
                            .fields(boostedFields));

                // Remove the quoted section from the keywords to continue processing
                rawKeywords = StringUtils.remove(rawKeywords, matcher.group(1));
            }


            if (StringUtils.isNotEmpty(rawKeywords)) {
                // Search for the combination of all keywords and a wildcard on the last one
                // (no custom fields in this one because it only works on text fields)
                query.should(multiMatchQuery(rawKeywords)
                                .type(MatchQuery.Type.PHRASE_PREFIX)
                                .fields(searchFields));

                String[] keywords = rawKeywords.split("\\s");
                if (ArrayUtils.isNotEmpty(keywords)) {
                    for (String keyword : keywords) {
                        query
                            // Search in the configured fields
                            .should(multiMatchQuery(keyword).fields(boostedFields))
                            // Search in the path, regex is required because the path is indexed as string
                            .should(regexpQuery(pathFieldName, keyword + ".*"));
                    }
                }
            }
        }

        if (StringUtils.isNotEmpty(params.getPath())) {
            query.filter(QueryBuilders.regexpQuery(pathFieldName, params.getPath()));
        }

        if(MapUtils.isNotEmpty(params.getFilters())) {
            updateFilters(query, params, siteFacets);
        }

        if (CollectionUtils.isNotEmpty(query.should()) &&
                (CollectionUtils.isNotEmpty(query.must()) || CollectionUtils.isNotEmpty(query.filter()))) {
            query.minimumShouldMatch(1);
        }

        SearchSourceBuilder builder = new SearchSourceBuilder()
            .query(query)
            .from(params.getOffset())
            .size(params.getLimit())
            .sort(getSortFieldName(params.getSortBy()), SortOrder.fromString(params.getSortOrder()));

        if(ArrayUtils.isNotEmpty(highlightFields)) {
            updateHighlighting(builder);
        }

        buildAggregations(builder, siteFacets);

        SearchRequest request = new SearchRequest()
            .source(builder);

        try {
            SearchResponse response = elasticsearchService.search(siteId, allowedPaths, request);
            return processResults(response, siteFacets);
        } catch (IOException e) {
            throw new ServiceLayerException("Error connecting to Elasticsearch", e);
        } catch (Exception e) {
            throw new ServiceLayerException("Error executing search in Elasticsearch", e);
        }
    }

    /**
     * Adds the aggregations needed to the given builder
     * @param builder the search source builder
     * @param siteFacets the facets from the site configuration
     */
    protected void buildAggregations(SearchSourceBuilder builder, Map<String, FacetTO> siteFacets) {
        Map<String, FacetTO> mergedFacets = new HashMap<>(facets);
        if(MapUtils.isNotEmpty(siteFacets)) {
            mergedFacets.putAll(siteFacets);
        }
        mergedFacets.forEach((name, facet) -> {
            if (facet.isRange() && facet.isDate()) {
                DateRangeAggregationBuilder aggregation = AggregationBuilders
                    .dateRange(name)
                    .field(facet.getField())
                    .keyed(true);
                for (FacetRangeTO range : facet.getRanges()) {
                    if (Objects.nonNull(range.getFrom()) && Objects.nonNull(range.getTo())) {
                        aggregation.addRange(range.getLabel(), range.getFrom(), range.getTo());
                    } else if (Objects.nonNull(range.getFrom())) {
                        aggregation.addUnboundedFrom(range.getLabel(), range.getFrom());
                    } else {
                        aggregation.addUnboundedTo(range.getLabel(), range.getTo());
                    }
                }

                builder.aggregation(aggregation);
            } else if (facet.isRange()) {
                RangeAggregationBuilder aggregation = AggregationBuilders
                    .range(name)
                    .field(facet.getField())
                    .keyed(true);
                for (FacetRangeTO range : facet.getRanges()) {
                    if (Objects.nonNull(range.getFrom()) && Objects.nonNull(range.getTo())) {
                        aggregation.addRange(range.getLabel(), Double.parseDouble(range.getFrom()),
                            Double.parseDouble(range.getTo()));
                    } else if (Objects.nonNull(range.getFrom())) {
                        aggregation.addUnboundedFrom(range.getLabel(), Double.parseDouble(range.getFrom()));
                    } else {
                        aggregation.addUnboundedTo(range.getLabel(), Double.parseDouble(range.getTo()));
                    }
                }

                builder.aggregation(aggregation);
            } else {
                builder.aggregation(AggregationBuilders
                    .terms(facet.getName())
                    .field(facet.getField())
                    .minDocCount(1)
                    .size(1000));
            }
        });
    }

    /**
     * Maps the field name from the configured facets, if its not found returns the same value.
     * @param name the facet name
     * @return name of the field to sort
     */
    protected String getSortFieldName(String name) {
        String field = name;
        if(facets.containsKey(field)) {
            field = facets.get(field).getField();
        }
        return field;
    }

    /**
     * Maps the Elasticsearch aggregations to {@link SearchFacet} objects
     * @param response the Elasticsearch response to map
     * @return the list of search facet objects
     */
    @SuppressWarnings("unchecked, rawtypes")
    private List<SearchFacet> processAggregations(final SearchResponse response, Map<String, FacetTO> siteFacets) {
        Map<String, FacetTO> mergedFacets = new HashMap<>(facets);
        if(MapUtils.isNotEmpty(siteFacets)) {
            mergedFacets.putAll(siteFacets);
        }
        List<SearchFacet> facets = new LinkedList<>();
        Aggregations aggregations = response.getAggregations();
        if(aggregations != null) {
            aggregations.getAsMap().forEach((name, aggregation) -> {
                SearchFacet facet = new SearchFacet();
                facet.setName(name);
                facet.setMultiple(mergedFacets.get(name).isMultiple());
                Map values = new LinkedHashMap();
                if(aggregation instanceof Terms) {
                    Terms terms = (Terms) aggregation;
                    for(Terms.Bucket bucket : terms.getBuckets()) {
                        values.put(bucket.getKey(), bucket.getDocCount());
                    }
                } else if(aggregation instanceof Range) {
                    Range range = (Range) aggregation;
                    for(Range.Bucket bucket : range.getBuckets()) {
                        SearchFacetRange rangeValues = new SearchFacetRange();
                        rangeValues.setCount(bucket.getDocCount());
                        try {
                            Instant instant = Instant.parse(bucket.getFromAsString());
                            LocalDate date = LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
                            rangeValues.setFrom(date.toString());
                            facet.setDate(true);
                        } catch (Exception e) {
                            rangeValues.setFrom(bucket.getFrom());
                        }
                        try {
                            Instant instant = Instant.parse(bucket.getToAsString());
                            LocalDate date = LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
                            rangeValues.setTo(date.toString());
                            facet.setDate(true);
                        } catch (Exception e) {
                            rangeValues.setTo(bucket.getTo());
                        }
                        values.put(bucket.getKey(), rangeValues);
                    }
                    facet.setRange(true);
                }
                if(MapUtils.isNotEmpty(values)) {
                    facet.setValues(values);
                    facets.add(facet);
                }
            });
        }
        return facets;
    }

    /**
     * Maps the item type for the given source based on the configuration
     * @param source the source to map
     * @return the item type
     */
    protected String getItemType(Map<String, Object> source) {
        if(MapUtils.isNotEmpty(types)) {
            for (HierarchicalConfiguration<ImmutableNode> typeConfig : types.values()) {
                String fieldName = typeConfig.getString(CONFIG_KEY_FIELD);
                if(source.containsKey(fieldName)) {
                    String fieldValue = source.get(fieldName).toString();
                    if (StringUtils.isNotEmpty(fieldValue) &&
                            fieldValue.matches(typeConfig.getString(CONFIG_KEY_TYPE_MATCHES))) {
                        return typeConfig.getString(CONFIG_KEY_NAME);
                    }
                }
            }
        }
        return defaultType;
    }

    /**
     * Maps the Elasticsearch highlighting to simple text snippets
     * @param highlights the highlighting to map
     * @return the list of snippets
     */
    protected List<String> getItemSnippets(Map<String, HighlightField> highlights) {
        if(MapUtils.isNotEmpty(highlights)) {
            List<String> snippets = new LinkedList<>();
            highlights.values().forEach(highlight -> {
                for(Text text : highlight.getFragments()) {
                    snippets.add(text.string());
                }
            });
            return snippets;
        }
        return null;
    }

    /**
     * Finds the mime type for the given item
     * @param source the item to map
     * @return the mime type
     */
    protected String getMimeType(Map<String, Object> source) {
        if(source.containsKey(mimeTypeName)) {
            return source.get(mimeTypeName).toString();
        } else {
            return DEFAULT_MIME_TYPE;
        }
    }

}

import { Modal } from 'antd';
import styled from 'styled-components';

const ModalHolder = styled(Modal)`
    .ant-modal-content {
        border-radius: 0;

        .ant-modal-header {
            padding: 4rem;

            .ant-modal-title {
                font-size: 4rem;
                letter-spacing: -.02em;
                font-weight: bold;
                text-align: center;
            }
        }

        .ant-modal-body {
            padding: 4rem;
        }

        .share-dialog__field{
            width: auto;
            width: 100%;
            margin: 0 0 2rem 0;
            padding: 1.5rem 2rem;
            height: 5rem;
            line-height: 5rem;
            font-size: 1.5rem;
            text-align: center;
            outline: none;
            border: 1px solid #ddd;
            color: #000
        }

        .share-dialog__social {
            display: -webkit-box;
            display: -ms-flexbox;
            display: flex;
            -ms-flex-wrap: wrap;
            flex-wrap: wrap;
        }

        .resp-sharing-button__link,
            .resp-sharing-button__icon {
            display: inline-block
        }

        .resp-sharing-button__link {
            text-decoration: none;
            color: #fff;
            width: 50%;

            @media (min-height: 20.625em) and (min-width: 28.125em){
                &:nth-child(2n-1) .resp-sharing-button {
                    margin-right: .5rem;
                }

                &:nth-child(2n) .resp-sharing-button {
                    margin-left: .5rem;
                }
            }
        }

        .resp-sharing-button {
            // border-radius: 5px;
            transition: 25ms ease-out;
            padding: 0.5em 0.75em;
            // font-family: Helvetica Neue,Helvetica,Arial,sans-serif
            font-family: inherit;
            font-size: 1.3rem;
            text-transform: uppercase;
            text-align: center;
            font-weight: bold;

            @media (min-height: 20.625em) {
                height: 5rem;
                line-height: 3.5rem;
                margin: 0 0 1rem;
            }
        }

        .resp-sharing-button__icon svg {
            width: 1em;
            height: 1em;
            margin-right: 0.4em;
            vertical-align: middle;
        }   

        .resp-sharing-button--small svg {
            margin: 0;
            vertical-align: middle
        }

        /* Non solid icons get a stroke */
        .resp-sharing-button__icon {
            stroke: #fff;
            fill: none
        }

        /* Solid icons get a fill */
        .resp-sharing-button__icon--solid,
        .resp-sharing-button__icon--solidcircle {
            fill: #fff;
            stroke: none
        }

        .resp-sharing-button--twitter {
            background-color: #55acee
        }

        .resp-sharing-button--twitter:hover {
            background-color: #2795e9
        }

        .resp-sharing-button--pinterest {
            background-color: #bd081c
        }

        .resp-sharing-button--pinterest:hover {
            background-color: #8c0615
        }

        .resp-sharing-button--facebook {
            background-color: #3b5998
        }

        .resp-sharing-button--facebook:hover {
            background-color: #2d4373
        }

        .resp-sharing-button--tumblr {
            background-color: #35465C
        }

        .resp-sharing-button--tumblr:hover {
            background-color: #222d3c
        }

        .resp-sharing-button--reddit {
            background-color: #5f99cf
        }

        .resp-sharing-button--reddit:hover {
            background-color: #3a80c1
        }

        .resp-sharing-button--google {
            background-color: #dd4b39
        }

        .resp-sharing-button--google:hover {
            background-color: #c23321
        }

        .resp-sharing-button--linkedin {
            background-color: #0077b5
        }

        .resp-sharing-button--linkedin:hover {
            background-color: #046293
        }

        .resp-sharing-button--email {
            background-color: #777
        }

        .resp-sharing-button--email:hover {
            background-color: #5e5e5e
        }

        .resp-sharing-button--xing {
            background-color: #1a7576
        }

        .resp-sharing-button--xing:hover {
            background-color: #114c4c
        }

        .resp-sharing-button--whatsapp {
            background-color: #25D366
        }

        .resp-sharing-button--whatsapp:hover {
            background-color: #1da851
        }

        .resp-sharing-button--hackernews {
            background-color: #FF6600
        }   
        .resp-sharing-button--hackernews:hover, .resp-sharing-button--hackernews:focus {   background-color: #FB6200 }

        .resp-sharing-button--vk {
            background-color: #507299
        }

        .resp-sharing-button--vk:hover {
            background-color: #43648c
        }

        .resp-sharing-button--facebook {
            background-color: #3b5998;
            border-color: #3b5998;
        }

        .resp-sharing-button--facebook:hover,
        .resp-sharing-button--facebook:active {
            background-color: #2d4373;
            border-color: #2d4373;
        }

        .resp-sharing-button--twitter {
            background-color: #55acee;
            border-color: #55acee;
        }

        .resp-sharing-button--twitter:hover,
        .resp-sharing-button--twitter:active {
            background-color: #2795e9;
            border-color: #2795e9;
        }

        .resp-sharing-button--google {
            background-color: #dd4b39;
            border-color: #dd4b39;
        }

        .resp-sharing-button--google:hover,
        .resp-sharing-button--google:active {
            background-color: #c23321;
            border-color: #c23321;
        }

        .resp-sharing-button--email {
            background-color: #777777;
            border-color: #777777;
        }

        .resp-sharing-button--email:hover,
        .resp-sharing-button--email:active {
            background-color: #5e5e5e;
            border-color: #5e5e5e;
        }   

    }
`;

export default ModalHolder;

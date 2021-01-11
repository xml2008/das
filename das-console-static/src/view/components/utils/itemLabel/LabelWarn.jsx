import React, {Component} from 'react'
import {Row, Col} from 'antd'
import './LabelWarn.less'
import _ from 'underscore'

export default class LabelWarn extends Component {

    static defaultProps = {
        show: '',
        title: '',
        labelCol: 5,
        style: {}
    }

    constructor(props, context) {
        super(props, context)
        this.state = {
            title: props.title
        }
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.title != this.state.title) {
            this.setState({title: nextProps.title})
        }
    }

    render() {
        const {title} = this.state
        const {labelCol} = this.props
        return (
            <Row style={{display: _.isEmpty(title) ? 'none' : 'block'}}>
                <Col sm={labelCol} className='base-col'/>
                <Col sm={24 - labelCol} className='end-col labelWarn' end style={{paddingLeft: '14px'}}>
                    {title}
                </Col>
            </Row>
        )
    }
}
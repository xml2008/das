import React from 'react'
import Component from '../base/Component'
import {Spin, Alert} from 'antd'
import './SpinPlus.less'
import _ from 'underscore'

export default class SpinPlus extends Component {

    static defaultProps = {
        size: 'small', //small default large
        type: 'info',
        tip: '',
        message: '',
        description: '',
        loading: false,
        isAlert: false
    }

    constructor(props, context) {
        super(props, context)
        this.states = props.states
        this.state = {
            loading: props.loading,
            isAlert: props.isAlert
        }
    }

    componentWillReceiveProps(nextProps) {
        const _state = {}
        if (nextProps.isAlert) {
            _.extend(_state, {loading: false})
        } else {
            if (nextProps.loading != this.state.loading) {
                _.extend(_state, {loading: nextProps.loading})
            }
        }
        if (nextProps.isAlert != this.state.isAlert) {
            _.extend(_state, {isAlert: nextProps.isAlert})
        }
        this.setState(_state)
    }

    render() {
        const {loading, isAlert} = this.state
        const {message, description, type, children, tip, size} = this.props
        return (
            <div className='spin-plus'>
                <Spin spinning={loading} tip={tip} size={size} style={{mimHeight: '500px'}}>
                    {children}
                </Spin>
                <Alert message={message} description={description} type={type}
                       style={{display: isAlert ? 'block' : 'none'}}/>
            </div>
        )
    }
}
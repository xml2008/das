/**
 * Created by liang.wang on 18/9/14.
 */
import React from 'react'
import {Row, Col, Form} from 'antd'
import {Row as ERow, Col as ECol} from 'eagle-ui'
import _ from 'underscore'
import './Itemlabel.less'
import LabelWarn from './LabelWarn'
import ManageCloudPanle from '../../pages/base/ManageCloudPanle'

export default class Itemlabel extends ManageCloudPanle {

    static defaultProps = {
        type: 0, //1、横排、其他，横排
        star: false,
        title: '',
        style: {},
        display: true,
        isValidate: false,
        value: null,
        labelStyle: {},
        //validateType: 1, //1.input 2. 单选 3.多选
        validatePath: 'validate',
        itemPath: 'item',
        formLayout: {},
        initMessage: () => {
        }
    }

    constructor(props, context) {
        super(props, context)
        const {modelName, children, title, display, isValidate, validatePath} = props
        this.formItemLayout = _.extend({
            labelCol: {
                xs: {span: 5},
                sm: {span: 6}
            },
            wrapperCol: {
                xs: {span: 24},
                sm: {span: 14}
            }
        }, props.formLayout)
        if (isValidate && children) {
            this.modelName = modelName
            this.initValueLink()
            //this.objName = this.modelName + '.' + props.itemPath
            this.valueLink = children.props.valueLink
            this.itemName = this.modelName + '.' + validatePath + '.' + this.valueLink.split('.').pop()
            this.state = {title, display, value: props.value}
            this.initvalidateMessage(props)
        } else {
            this.state = {title, display}
        }
    }

    initvalidateMessage = props => {
        const {title, curValueLink, isValidate, children} = props
        if (children && isValidate && curValueLink === '') {
            const valueLink = children.props.valueLink
            if (!_.isEmpty(valueLink) && valueLink.includes('.')) {
                const key = valueLink.split('.').pop()
                let validateMsg = '请选择' + title + '!'
                if (children && children.type.defaultProps.type === 'text') {
                    validateMsg = '请输入' + title + '!'
                }
                this.props.initMessage(key, validateMsg)
            }
        }
    }

    componentWillReceiveProps(nextProps) {
        const {display, title, curValueLink, isValidate, value} = nextProps
        if (display != this.state.display || title != this.state.title) {
            this.setState({display, title})
        }

        if (isValidate && curValueLink != undefined && this.valueLink === curValueLink) {
            if (value != this.state.value) {
                this.setState({value}, () => {
                    this.runValidate(curValueLink)
                })
            }
        }
    }

    runValidate = curValueLink => {
        const {validatePath} = this.props
        if (this.valueLink === curValueLink) {
            const key = curValueLink.split('.').pop()
            this.validate({key, path: validatePath})
        }
    }

    render() {
        let errMsg = ''
        const {type, star, children, isValidate, title, labelStyle} = this.props
        const {display} = this.state
        const style = _.extend({marginBottom: '10px'}, this.props.style)
        const style2 = _.extend({marginBottom: '0px'}, this.props.style)
        if (!children || !display) {
            return null
        }

        if (isValidate) {
            const obj = this.getValueToJson(this.itemName)
            if (obj && !_.isEmpty(obj.s)) {
                errMsg = obj.s
            }
            if (obj && obj.s != undefined) {
                this.formItemLayout = _.extend(this.formItemLayout, {validateStatus: _.isEmpty(errMsg) ? 'success' : 'error'})
            }
        }

        if (type === 1) {
            return <div className='itemlabel'>
                <ERow className='pad-row'>
                    <ECol sm={3} className='base-col'>
                        <span className='spanInline'>{title}:</span>
                        {star ? <span className='redFont spanInline'>*</span> : null}
                    </ECol>
                    <ECol sm={9} end>
                        {children}
                    </ECol>
                </ERow>
            </div>
        } else if (type === 2) {
            const FormItem = Form.Item
            const stars = star ? <span style={{color: 'red'}}>*</span> : null
            const label = <div className='label-div' style={labelStyle}>
                <div style={{margin: '2px 3px 0px 0px'}}>{stars}</div>
                <div>{title}</div>
            </div>
            return <div className='itemlabel' style={style}>
                <FormItem {...this.formItemLayout} label={label} style={style2} colon={false} hasFeedback>
                    {children}
                </FormItem>
                <LabelWarn title={errMsg} labelCol={this.formItemLayout.labelCol.sm}/>
            </div>
        }
        return <Row style={style}>
            <Col>{star ? <span className='redFont spanInline' style={{color: 'red'}}>*</span> : null}{title}</Col>
            <Col sm={24}>
                {children}
            </Col>
        </Row>
    }
}

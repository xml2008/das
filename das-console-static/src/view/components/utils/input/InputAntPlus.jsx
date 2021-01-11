/**
 * Created by liang.wang on 16/9/29.
 */
import React, {PropTypes} from 'react'
import Component from '../base/Component'
import {Input} from 'antd'
import {DataUtil} from '../util/Index'
import './InputPlus.less'
import _ from 'underscore'

export default class InputAntPlus extends Component {
    static propTypes = {
        /**
         * 是否只读
         */
        viewOnly: PropTypes.bool,
        /**
         * 是否disabled
         */
        disabled: PropTypes.bool,
        /**
         * value链接
         */
        valueLink: PropTypes.string.isRequired,
        /**
         * 初始化数值
         */
        defaultValue: PropTypes.string.isRequired
    }
    /**
     * @type {{viewOnly: boolean, span: boolean, disabled: boolean, className: string, placeholder: string, valueLink: string, defaultValue: string, validRules: {isInt: boolean, isFloat: boolean, maxLength: null}, style: {}, onChange: InputPlus.defaultProps.onChange, onBlurCallback: InputPlus.defaultProps.onBlurCallback}}
     * 优先级 优先级 viewOnly（span） ---> disabled
     */
    static defaultProps = {
        type: 'text',
        viewOnly: false,
        span: false,
        disabled: false,
        className: 'f14 font',
        placeholder: '',
        valueLink: '',
        value: null,
        defaultValue: '',
        validNeed: true,
        validRules: {
            isDbName: false,            //英文小写和下划线
            isEnglishnderline: false,   //英文和下划线
            isClassName: false,         //类名
            isInt: false,
            isFloat: false,
            isProjectName: false,
            maxLength: null
        },
        style: {},
        onBlurCallback: function () {
        },
        onChangeCallBack: function () {
        },
        onSetValueValidate: null
    }

    constructor(props, context) {
        super(props, context)
        this.state = {
            disabled: props.disabled,
            viewOnly: props.viewOnly,
            content: props.defaultValue || props.value
        }
    }

    componentWillReceiveProps(nextProps) {
        let _state = {}
        if (nextProps.disabled != this.props.disabled || nextProps.viewOnly != this.props.viewOnly) {
            _.extend(_state, {
                disabled: nextProps.disabled,
                viewOnly: nextProps.viewOnly
            })
        }
        if (nextProps.defaultValue != this.props.defaultValue) {
            _.extend(_state, {content: nextProps.defaultValue})
        }
        if (nextProps.value != null && nextProps.value != this.state.content) {
            _.extend(_state, {content: nextProps.value})
        }
        this.setState(_state)
    }

    change(val) {
        const {valueLink, onSetValueValidate} = this.props
        val = this.validData(val)
        if (!_.isEqual(this.state.content, val)) {
            valueLink && this.setValueByReducers(val)
            this.setState({
                content: val
            }, () => {
                onSetValueValidate && onSetValueValidate(val, this)
            })
        }
    }

    onChangeHandler(e) {
        const {validNeed} = this.props
        let val = validNeed ? DataUtil.StringUtils.trim(e.target.value) || '' : e.target.value
        this.change(val)
        this.props.onChangeCallBack && this.props.onChangeCallBack(val, this)
    }

    onblurHandler(e) {
        const {validNeed} = this.props
        let val = validNeed ? DataUtil.StringUtils.trim(e.target.value) || '' : e.target.value
        this.change(val)
        this.props.onBlurCallback && this.props.onBlurCallback(val, this)
    }

    validData(val) {
        const {validNeed} = this.props
        if (!validNeed) {
            return val
        }
        if (this.props.validRules.maxLength)
            val = DataUtil.StringUtils.getLength(val, this.props.validRules.maxLength)
        if (this.props.validRules.isInt)
            val = DataUtil.StringUtils.getInt(val)
        if (this.props.validRules.isFloat)
            val = DataUtil.StringUtils.getFloat(val)
        if (this.props.validRules.isDbName)
            val = DataUtil.StringUtils.getDbName(val)
        if (this.props.validRules.isEnglishnderline)
            val = DataUtil.StringUtils.getEnglishnderline(val)
        if (this.props.validRules.isClassName)
            val = DataUtil.StringUtils.getClassName(val)
        if (this.props.validRules.isProjectName)
            val = DataUtil.StringUtils.getProjectName(val)
        return val
    }

    render() {
        const _this = this
        const {type} = this.props
        const {disabled} = this.state
        if (this.state.viewOnly) {
            if (this.state.span) {
                return (
                    <div className='inputPlus'>
                        <span>{this.state.content}</span>
                    </div>
                )
            } else {
                return (
                    <div className='inputPlus'>
                        <Input type={type} value={this.state.content}
                               ref={(ref) => {
                                   _this.setDisabled(ref, true)
                               }}/>
                    </div>
                )
            }
        } else {
            return (
                <Input style={this.props.style}
                       disabled={disabled}
                       className={this.props.className} type={type} value={this.state.content}
                       placeholder={this.props.placeholder}
                       onChange={::this.onChangeHandler}
                       onBlur={::this.onblurHandler}/>
            )
        }
    }
}

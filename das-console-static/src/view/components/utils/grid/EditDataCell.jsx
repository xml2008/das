/**
 * Created by liang.wang on 20/03/7.
 */
import React, {Component} from 'react'
import {Icon, Input} from 'antd'
import './EditDataCell.less'

export default class EditDataCell extends Component {

    static defaultProps = {
        _key: 'key',
        item: {}
    }

    constructor(props, context) {
        super(props, context)
        this.state = {
            value: props.value,
            editable: false
        }
    }

    handleChange = e => {
        const value = e.target.value
        this.setState({value})
    }

    check = () => {
        const {value} = this.state
        this.setState({editable: false})
        const {_key, item, onChange} = this.props
        item[_key] = value
        onChange && onChange(item, value)

    }

    edit = () => {
        this.setState({editable: true})
    }

    render() {
        const {value, editable} = this.state
        return (
            <div className='editData-cell'>
                <div className='editable-cell'>
                    {
                        editable ? <div className='editable-cell-input-wrapper'>
                                <Input value={value} onChange={this.handleChange} onPressEnter={this.check}/>
                                <Icon type='check' className='editable-cell-icon-check' onClick={this.check}/>
                            </div>
                            :
                            <div className='editable-cell-text-wrapper'>
                                {value || ' '}
                                <Icon type='edit' className='editable-cell-icon' onClick={this.edit}/>
                            </div>
                    }
                </div>
            </div>
        )
    }
}
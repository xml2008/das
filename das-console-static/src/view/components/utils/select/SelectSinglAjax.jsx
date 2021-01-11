/**
 * Created by liang.wang on 20/03/17.
 * 多选
 * valueLink 更新item
 */
import React from 'react'
import Component from '../base/Component'
import {Select, Spin} from 'antd'
import {DataUtil} from '../util/Index'
import _ from 'underscore'

export default class SelectSinglAjax extends Component {

    static defaultProps = {
        valueLink: '',
        selectedId: '',
        defaultValue: '',
        items: [],              //[{id: 1, name: 'mySql'}, {id: 2, name: 'sqlServer'}]
        format: {id: 'id', name: 'name'},
        idIsNumber: true,       //id是否是数字
        placeholder: '',
        disabled: false,
        style: {width: '100%'},
        size: 'large',
        fetching: false,
        allowClear: false,
        showSearch: true,
        onSearchCallback: () => { //单选 文本框值变化时回调
        },
        onChangeCallbBack: () => { //单选
        },
        setSelectItemList: itemList => {
            return itemList
        },
        onSetValueValidate: null
    }

    constructor(props, context) {
        super(props, context)
        const {items, selectedId} = props
        this.format = props.format
        const _items = DataUtil.ObjUtils.transformJson(items, this.format)
        this.items = _items
        this.defaultValue = this.getDefaultValue(props, _items)
        this.state = {
            fetching: props.fetching,
            items: _items,
            //单选
            selectedId: selectedId,
            selectedValue: this.defaultValue
        }
    }

    componentWillReceiveProps(nextProps) {
        const {items, selectedId} = nextProps
        const _items = DataUtil.ObjUtils.transformJson(items, this.format)
        //单选
        if (!DataUtil.ObjUtils.isEqual(this.state.items, _items) || this.state.selectedId != selectedId) {
            const selectedValue = this.getDefaultValue(nextProps, _items)
            this.setState({items: _items, selectedId, selectedValue})
        }
    }

    getDefaultValue = (props, items) => {
        let {selectedId, idIsNumber} = props
        //单选
        if (!_.isEmpty(items)) {
            selectedId = idIsNumber ? parseInt(selectedId) : String(selectedId)
            return DataUtil.ObjUtils.findWhereValById(items, {id: selectedId}, 'name')
        }
        return null
    }

    getIdByName = name => {
        const {items} = this.state
        return DataUtil.ObjUtils.findWhereIdByVal(items, {name}, 'id')
    }

    toId = id => {
        const {idIsNumber} = this.props
        if (id == undefined || id == 'undefined') {
            return ''
        }
        return idIsNumber ? parseInt(id) : String(id)
    }

    getNameById = id => {
        const {items} = this.state
        id = this.toId(id)
        const name = DataUtil.ObjUtils.findWhereIdByVal(items, {id}, 'name')
        return name == undefined ? '' : name
    }

    createOptions = items => {
        let options = []
        if (DataUtil.is.Array(items)) {
            _.each(items, item => {
                options.push(<Select.Option title={String(item.id)} value={String(item.id)}
                                            key={item.id}>{item.name}</Select.Option>)
            })
        }
        return options
    }

    onChange = id => {
        const {valueLink, onChangeCallbBack, onSetValueValidate} = this.props
        id = this.toId(id)
        const selectedValue = this.getNameById(id)
        valueLink && this.setValueByReducers(id)
        this.setState({selectedValue}, () => {
            onChangeCallbBack && onChangeCallbBack(id, selectedValue, this)
            onSetValueValidate && onSetValueValidate(String(id), this)
        })
    }

    render() {
        const {style, disabled, size, mode, placeholder, onSearchCallback, allowClear, showSearch} = this.props
        const {items, selectedValue, fetching} = this.state
        const _props = {size, disabled, mode, placeholder, allowClear}
        return (
            <Select {..._props} style={style} showSearch={showSearch} placeholder={placeholder} value={selectedValue}
                    notFoundContent={fetching ? <Spin size='small'/> : null}
                    onChange={this.onChange} onSearch={e => onSearchCallback(e)}
                    filterOption={(input, option) => option.props.children && option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}>
                {this.createOptions(items)}
            </Select>
        )
    }
}
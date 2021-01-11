/**
 * Created by liang.wang on 17/9/7.
 */
import React from 'react'
import Component from '../base/ComponentAlert'
import {Row, Col, PanelHeader, PanelContent} from 'eagle-ui'
import DataRow from './DataRow'
import DataPagination from './DataPagination'
import classNames from 'classnames'
import {imgs} from '../table/img/imgs'
import Immutable from 'immutable'
import QueueAnim from 'rc-queue-anim'
import {DataUtil, CoreUtil} from '../util/Index'
import {Button, DatePicker, Input, Popconfirm, Popover, Tooltip} from 'antd'
import {CheckboxGroupPlus, CodeEditor} from '../Index'
import Empty from './Empty'
import './DataRow.less'
import './DataList.less'
import {dataListConfig} from '../../../../model/base/BaseModel'
import EditDataCell from './EditDataCell'
import _ from 'underscore'

/**
 * div table
 */
export default class DataList extends Component {

    static defaultProps = {
        modelName: 'modelName',
        columnInfo: {},
        theadList: [],
        isPagination: true, //是否分页
        isUseList: false,
        checkBoxMulti: false,
        doubleShow: false,
        isSarchRow: true,
        isSearchAble: false,
        showSizeChanger: true,
        marginStyle: {position: 'relative', overflow: 'auto'},
        panelStyle: {overflow: 'auto', minWidth: '1200px'},
        searchInfo: {totalCount: 0, pageSize: 10, page: 1},
        createSeartchParam: searchInfo => {
            return searchInfo
        },
        onChangeRangePickerCallback: function () {
        },
        searchOnChangeCallback: function () {
        },
        clearSearchCallback: function () {
        },
        sortBack: function () {
        },
        onCellChange: function () {
        },
        editorItem: function () {
        },
        deleteItem: function () {
        },
        loadData: function () {
        },
        ceatTitle: null,
        createTd: {
            /*type: (itemLabel, ele, key, index) => {
                return index
            }*/
        },
        createTdButton: {
            /* type: (itemLabel, ele, key, index) => {
                 return index
             }*/
        },
        initValueLink: (_this, modelName) => {
            _this.searchResultList = modelName + dataListConfig.searchResultList
            _this.searchInfo = modelName + dataListConfig.searchInfo
            _this.columnInfo = modelName + dataListConfig.columnInfo
            _this.columnInfoBack = modelName + dataListConfig.columnInfoBack
        }
    }

    constructor(props, context) {
        super(props, context)
        const {modelName} = props
        this.initValueLink(modelName)
        this.state = {
            expand: 'up',
            refreshRow: false,
            toastType: 'success',
            toastMsg: '',
            isShowSarch: false,
            isSarchRow: props.isSarchRow,
            theadList: [],
            searchInfo: props.searchInfo
        }
    }

    componentWillReceiveProps(nextProps) {
        const {isUseList, theadList, searchInfo} = nextProps
        if (isUseList) {
            if (!DataUtil.ObjUtils.isEqual(theadList, this.state.theadList)) {
                this.setState({theadList})
            }
            if (!_.isEmpty(searchInfo) && !DataUtil.ObjUtils.isEqual(searchInfo, this.state.searchInfo)) {
                this.setState({searchInfo})
            }
        }
    }

    initValueLink = modelName => {
        this.props.initValueLink(this, modelName)
    }

    toggle(num, item) {
        const columnInfo = this.getValueToJson(this.columnInfo)
        if (!columnInfo.column[num].key) {
            return
        }
        const addSelected = (classNametitle) => {
            if (classNametitle.indexOf('col-color-select') > -1) {
                return classNametitle
            }
            return classNametitle + 'col-color-select'
        }
        const removeSelected = (classNametitle) => {
            return classNametitle.replace('col-color-select', '')
        }

        for (const i in columnInfo.column) {
            if (num == i) {
                columnInfo.column[i].classNametitle = addSelected(columnInfo.column[i].classNametitle)
                columnInfo.column[i].classNameColumn = addSelected(columnInfo.column[i].classNameColumn)
                columnInfo.column[i].sortArrow = this.toggleSort(columnInfo.column[i].key, columnInfo.column[i].sortArrow, item)
            } else {
                columnInfo.column[i].classNametitle = removeSelected(columnInfo.column[i].classNametitle)
                columnInfo.column[i].classNameColumn = removeSelected(columnInfo.column[i].classNameColumn)
                columnInfo.column[i].sortArrow = 'both'
            }
        }
        this.setState({columnInfo: Immutable.fromJS(columnInfo)})
    }

    toggleSort(key, sort, item) {
        let _sort = 'sort'

        switch (sort) {
            case 'both':
                _sort = 'asc'
                break
            case 'asc':
                _sort = 'desc'
                break
            case 'desc':
                _sort = 'asc'
                break
        }
        this.props.sortBack && this.props.sortBack(key, _sort, item, this)
        return _sort
    }

    ceatTitle = item => {
        if (this.props.ceatTitle) {
            return this.props.ceatTitle(item)
        }
        const getImageStyles = (type) => {
            switch (type) {
                case 'both':
                    return imgs.SORT_BOTH
                case 'asc':
                    return imgs.SORT_ASC
                case 'desc':
                    return imgs.SORT_DESC
            }
        }
        const imgsrc = getImageStyles(item.sortArrow)
        if (item.name == '操作' || item.type === 'sequence') {
            const {isShowSarch} = this.state
            const {isSearchAble} = this.props
            return (<p>{isSearchAble ? <Tooltip placement='right' title='展开或隐藏查询条件'>
                <Button size='small' onClick={() => {
                    this.setState({isShowSarch: !isShowSarch})
                }} icon='search'/>
            </Tooltip> : item.name}</p>)
        } else {
            if (item.sort) {
                return (<p>{item.name}<img src={imgsrc} style={{position: 'absolute', top: '7px'}}/></p>)
            }
        }
        return item.name
    }

    createTd = (item, ele, index) => {
        if (item.type === 'sequence') {
            return index
        }
        const {createTd} = this.props
        for (var key in createTd) {
            if (item.key.toLocaleLowerCase() === key.toLocaleLowerCase()) {
                return createTd[key](item, ele, key, index)
            }
        }
        if (!_.isEmpty(item.dateFormat)) {
            return new Date(ele[item.key]).format(item.dateFormat)
        }
        if (item.link) {
            return <Tooltip placement='top' title={item.title}>
                <a href={window.location.origin + '/#/' + item.link.url + ele[item.key]}
                   target='_blank'>{ele[item.key]}</a>
            </Tooltip>
        }
        if (item.popover || item.map) {
            return this.createPopover(item, ele)
        }
        if (item.popoveTags) {
            return this.createPopoverTag(item, ele)
        }
        if (item.type === 'editor') {
            return <EditDataCell _key={item.key} item={ele} value={ele[item.key]} onChange={this.props.onCellChange}/>
        }
        /*  if (itemLabel.html) {
              return <div style={{backgroundColor: '#fff'}} dangerouslySetInnerHTML={{__html: ele[itemLabel.key]}}/>
          }*/
        if (item.key) {
            const content = CoreUtil.store.getJsonValeByKey(ele, item.key)
            return content && content.toString ? content.toString() : content
        }
        return '-'
    }

    /**
     *  popover: {title: '标题', content: '显示内容:可选,有则显示按钮，没有则显示内容', maximum: '最多显示字数', placement: '提示框的位置'}
     *  map: map: {1: 'key1', 2: 'key2'},
     */
    createPopover = (item, ele) => {
        const createShow = (item, value, type, maximum) => {
            if (!DataUtil.StringUtils.isEmpty(value) && value.length > maximum) {
                value = value.substring(0, maximum) + '...'
            }
            const content = item.popover.content ? item.popover.content : value
            if (type === 1) {
                return <Button type='dashed'>{content}</Button>
            } else if (type === 2) {
                return <a type='dashed'>{content}</a>
            }
        }
        const content = CoreUtil.store.getJsonValeByKey(ele, item.key)
        let value = item.key ? (DataUtil.StringUtils.isEmpty(content) ? '-' : content) : '-'
        if (item.popover) {
            if (item.popover.html) {
                /*const content = <div style={{backgroundColor: '#fff'}} dangerouslySetInnerHTML={{__html: value}}/>
                return <Popover placement='top' title='调度备注' content={content} trigger='click'>
                    <p style={{color: itemLabel.popover.color, cursor: 'pointer'}}>查看</p>
                </Popover>*/
            }
            const maximum = item.popover.maximum ? item.popover.maximum : 15
            if (value.length > maximum) {
                if (item.popover.type) {
                    value = <CodeEditor style={{width: '1000px', height: '400px'}}
                                        contStyle={{width: '1000px', height: '400px'}}
                                        mode={item.popover.type} theme='monokai' value={value}/>
                }
                const placement = item.popover.placement ? item.popover.placement : 'bottom'
                const show = item.popover.content ? createShow(item, value, 1, maximum) : createShow(item, value, 2, maximum)
                const content = CoreUtil.createContent(value)
                return <Popover placement={placement} content={content} title={item.popover.title}>
                    {show}
                </Popover>
            }
        }
        return item.map ? DataUtil.StringUtils.isEmpty(item.map[content]) ? '-' : item.map[content] : value
    }
    /**
     * popoveTags: {title: 'OnLine 机器', type: 'tag', color: 'green', dataType: 'arr', separator:',', rowSize:5, maximum:15}
     */
    createPopoverTag = (item, ele) => {
        const config = _.extend({
            type: 'tag', color: 'green', dataType: 'arr',
            separator: ',', rowSize: 5, maximum: 15
        }, item.popoveTags)
        const createShow = (item, value, type, maximum) => {
            if (!DataUtil.StringUtils.isEmpty(value) && value.length > maximum) {
                value = value.substring(0, maximum) + '...'
            }
            const content = config.content ? config.content : value
            if (type === 1) {
                return <Button type='dashed'>{content}</Button>
            } else if (type === 2) {
                return <a type='dashed'>{content}</a>
            }
        }
        let arr = [], val = '',
            value = item.key ? (_.isEmpty(ele[item.key]) ? '-' : ele[item.key]) : '-'
        if (config.dataType === 'arr') {
            arr = value
            val = ele[item.key] ? ele[item.key].join(',') : ''
        } else {
            val = value
            if (value.includes(config.separator)) {
                arr = value.split(config.separator)
            }
        }
        const maximum = config.maximum
        const content = DataUtil.is.Array(arr) && !_.isEmpty(arr) ? CoreUtil.createTaqgs(item, arr) : value
        if (val.length > maximum) {
            const placement = config.placement ? config.placement : 'bottom'
            const show = config.content ? createShow(item, value, 1, maximum) : createShow(item, val, 2, maximum)
            return <Popover placement={placement} content={content} title={config.title}>
                {show}
            </Popover>
        } else {
            return content
        }
    }

    /**
     * {name: '操作', width: 10,key: null, button: [{type: 'editor', title: '编辑分组', icon: false},{type: 'delete', text: '确认删除分组?', title: '删除分组', icon: true}] }
     * @param column
     * @param ele
     * @param index
     * @returns {*}
     */
    createTdButton(column, ele, index) {
        const {deleteItem, editorItem, createTdButton} = this.props
        return <table className='buttonTable' style={{width: '5%', margin: '0 auto'}}>
            <tbody>
            <tr>
                {
                    Object.keys(createTdButton) && Object.keys(createTdButton).map((key, i) => {
                        const func = createTdButton[key]
                        if (func) {
                            return <td style={{paddingRight: '8px'}} key={i}>
                                {func(ele, key, index)}
                            </td>
                        }
                    })
                }
                {
                    column.button && column.button.map && column.button.map((item, i) => {
                        if ('editor' == item.type) {
                            return <td style={{paddingRight: '8px'}} key={i}>
                                {item.icon ?
                                    <Tooltip placement='top' title={item.title}>
                                        <Button icon='edit' size='small' onClick={e => editorItem(ele, e, item)}/>
                                    </Tooltip> :
                                    <span style={{paddingRight: '8px'}} key={i}>
                                        <Button size='small' type={item.type}
                                                onClick={e => editorItem(ele, e, item)}>{item.title}</Button>
                                    </span>}
                            </td>
                        }
                        if ('delete' == item.type) {
                            return <td style={{paddingRight: '8px'}} key={i}>
                                <Popconfirm placement='top' title={item.text} okText='是' cancelText='否'
                                            onConfirm={e => deleteItem(ele, e, item)}>
                                    {item.icon ?
                                        <Tooltip placement='top' title={item.title}>
                                            <Button icon='delete' size='small' type='danger'/>
                                        </Tooltip> :
                                        <Button size='small' type={item.type}>{item.title}</Button>}
                                </Popconfirm>
                            </td>
                        }
                    })
                }
            </tr>
            </tbody>
        </table>
    }

    searchOnChange = (v, item) => {
        this.props.searchOnChangeCallback && this.props.searchOnChangeCallback(1, v, item)
    }

    clearSearch = () => {
        this.setState({isSarchRow: false}, () => {
            this.setState({isSarchRow: true})
            this.props.clearSearchCallback && this.props.clearSearchCallback()
        })
    }

    checkboxCallBack = (key, checkAll, checkeds) => {
        this.props.searchOnChangeCallback && this.props.searchOnChangeCallback(2, key, checkAll, checkeds)
    }

    onChangeRangePicker = (date, dateString, item) => {
        this.props.onChangeRangePickerCallback && this.props.onChangeRangePickerCallback(dateString, item)
    }

    createSearch = item => {
        if (!item.button) {
            if (item.checkbox || item.type) {
                return null
            } else if (item.map) {
                return <Popover placement='left' title={item.name} trigger='click'
                                content={<CheckboxGroupPlus checkboxCallBack={::this.checkboxCallBack}
                                                            options={item.map} keyType={item.key}/>}>
                    <Button icon='filter' size='small'/>
                </Popover>
                /*<SelectPlus items={itemLabel.map} mode='multiple' valueLink={this.objName + '.items'}/>*/
            } else if (item.timePicker) {
                const {RangePicker} = DatePicker
                return <Popover placement='left' title={item.name} trigger='click'
                                content={<RangePicker
                                    onChange={(date, dateString) => ::this.onChangeRangePicker(date, dateString, item)}/>}>
                    <Button icon='calendar' size='small'/>
                </Popover>
            } else if (item.search != false) {
                return <Input placeholder='查询...'
                              onChange={e => ::this.searchOnChange(e.target.value, item)}
                              onPressEnter={e => ::this.searchOnChange(e.target.value, item)}/>
            }
        }
        else {
            return <Button icon='reload' size='small' onClick={::this.clearSearch}>查询条件重置</Button>
        }
    }

    loadPageCallback = pageSize => {
        const {isUseList} = this.props
        const searchInfo = isUseList ? this.state.searchInfo : this.getValueToJson(this.searchInfo)
        searchInfo.page = 1
        searchInfo.pageSize = pageSize
        if (!isUseList) {
            this.setValueToImmutable(this.searchInfo, searchInfo)
        }
        this.props.loadData && this.props.loadData(this.props.createSeartchParam(searchInfo))
    }

    pageCallback = page => {
        const {isUseList} = this.props
        const searchInfo = isUseList ? this.state.searchInfo : this.getValueToJson(this.searchInfo)
        searchInfo.page = page
        if (!isUseList) {
            this.setValueToImmutable(this.searchInfo, searchInfo)
        }
        this.props.loadData && this.props.loadData(this.props.createSeartchParam(searchInfo))
    }

    getList = () => {
        const {isUseList} = this.props
        if (isUseList) {
            return this.state.theadList
        }
        return this.getValueToJson(this.searchResultList)
    }

    getSearch = () => {
        const {isUseList} = this.props
        if (isUseList) {
            return this.state.searchInfo
        }
        return DataUtil.ObjUtils.filterObjByKeys(this.getValueToJson(this.searchInfo), ['totalCount', 'page', 'pageSize'])
    }

    render() {
        const list = this.getList()
        const {isSearchAble, setValueByReducers, isPagination, marginStyle, panelStyle, showSizeChanger, doubleShow} = this.props
        const {isShowSarch, isSarchRow} = this.state
        const columnInfo = this.getValueToJson(this.columnInfo)
        const search = this.getSearch()
        const createTdButton = ::this.createTdButton, createTd = this.createTd, ceatTitle = this.ceatTitle
        const _props = {createTdButton, createTd, ceatTitle, columnInfo, list, search, setValueByReducers, isPagination}
        return (
            <div>
                <div className='tradeList outerPanel marginTopSpace' style={marginStyle}>
                    <div className='marginTopSpace' style={panelStyle}>
                        <PanelHeader className='marginSpacePanelHeader'>
                            <div className='divTable paleTable'>
                                <QueueAnim className='divTableBody' animConfig={[
                                    {opacity: [2, 0], translateY: [0, 80]},
                                    {opacity: [2, 0], translateY: [0, -80]}
                                ]} duration={240}>
                                    <div className='divTablelistRow'>
                                        {
                                            columnInfo.column && columnInfo.column.map((item, i) => {
                                                const _className = classNames(item.sortArrow + '_style', item.classNametitle, 'divTableCell')
                                                //const imgsrc = this.getImageStyles(itemLabel.sortArrow)
                                                /*  return <Col style={{width: itemLabel.width + '%'}}
                                                              className={_className}
                                                              onClick={() => {
                                                                  itemLabel.sort ? this.toggle(i, itemLabel) : null
                                                              }} key={i}> {::this.ceatTitle(itemLabel)}
                                                  </Col>*/
                                                return <div className={_className} key={i}
                                                            style={{
                                                                width: item.width + '%',
                                                                /*fontSize: '15px',*/
                                                                color: 'rgba(0,0,0,.85)',
                                                                fontWeight: 500
                                                            }}
                                                            onClick={() => {
                                                                item.sort ? this.toggle(i, item) : null
                                                            }}>
                                                    {::this.ceatTitle(item)}
                                                </div>
                                            })
                                        }  </div>
                                </QueueAnim>
                            </div>
                            {
                                (isSearchAble && isSarchRow) ?
                                    <Row className='panelHeader' style={{display: isShowSarch ? 'block' : 'none'}}>
                                        {
                                            columnInfo && columnInfo.get('column').map((item, i) => {
                                                item = item.toJS()
                                                const _className = classNames(item.sortArrow + '_style', item.classNametitle)
                                                return <Col style={{width: item.width + '%'}} className={_className}
                                                            key={i}>
                                                    {::this.createSearch(item)}
                                                </Col>
                                            })
                                        }
                                    </Row> : null
                            }
                        </PanelHeader>
                        <PanelContent style={{'padding-top': '0px'}}>
                            {_.isEmpty(list) ? <Empty/> : <DataRow {..._props}/>}
                        </PanelContent>
                    </div>
                </div>
                {_.isEmpty(list) ? null : <DataPagination {..._props} pageCallback={::this.pageCallback}
                                                          doubleShow={doubleShow}
                                                          showSizeChanger={showSizeChanger}
                                                          loadPageCallback={::this.loadPageCallback}/>}
            </div>
        )
    }
}
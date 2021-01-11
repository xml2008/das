/**
 * Created by liang.wang on 17/9/7.
 */
import React from 'react'
import Component from '../base/ComponentAlert'
import {Row, Col, Paging} from 'eagle-ui'
import {Button} from 'antd'
import {DataUtil} from '../util/Index'
import $ from 'jquery'

/**
 * 分页
 * {size:large/small, align:l/m/r; border:true/false}
 */
export default class DataPagination extends Component {

    static defaultProps = {
        isPagination: true,
        showSizeChanger: true,
        doubleShow: false,
        search: {
            totalCount: 0,
            page: 1,
            pageSize: 20
        }
    }

    constructor(props, context) {
        super(props, context)
        this.state = {
            search: props.search,
            isPagination: props.isPagination,
            defautDoubleShow: props.doubleShow,
            doubleShow: props.doubleShow
        }
        this.storageCode = 'das_console_dataPagination_storageConfig'
        this.defaultData = {size: 'small', align: 'm', border: false} //large,small
    }

    componentWillReceiveProps(nextProps) {
        const {isPagination, search} = nextProps
        if (isPagination && !DataUtil.ObjUtils.isEqual(search, this.state.search)) {
            this.setState({isPagination, search})
        }
    }

    setTimeoutInitData = () => {
        this.timer = 3
        setTimeout(() => {
            this.initData()
            const interval = setInterval(() => {
                this.timer--
                if (this.timer > 0) {
                    this.initData()
                } else {
                    clearInterval(interval)
                }
            }, 300)
        }, 150)
    }

    componentDidMount() {
        setTimeout(() => {
            this.setTimeoutInitData()
        }, 1000)
    }

    initData = () => {
        const config = DataUtil.getLocalStorageData(this.storageCode, this.defaultData)
        if (config.size === 'small') {
            this.small()
        } else {
            this.large()
        }
        if (config.align === 'l') {
            this.left()
        } else if (config.align === 'm') {
            this.center()
        } else if (config.right === 'r') {
            this.right()
        }
        if (config.border === true) {
            this.exist()
        } else {
            this.none()
        }
    }

    loadPageCallback(ps) {
        this.setState({
            search: {
                page: 1,
                pageSize: parseInt(ps)
            }
        }, () => {
            this.props.loadPageCallback && this.props.loadPageCallback(parseInt(ps), this)
            this.setTimeoutInitData()
        })
    }

    pageCallback(page) {
        this.setState({
            search: {
                page: page,
                pageSize: this.state.search.pageSize
            }
        }, () => {
            this.props.pageCallback && this.props.pageCallback(page, this)
            this.setTimeoutInitData()
        })
    }

    updateLocalStorageData = (key, val) => {
        const config = DataUtil.getLocalStorageData(this.storageCode, this.defaultData)
        config[key] = val
        DataUtil.setLocalStorageData(this.storageCode, config)
    }

    large = (flag = false) => {
        $('.divTableRow').css({'height': '46px'})
        $('.divTablelistRow').css({'height': '46px'})
        $('.divTableCell').css({'font-size': '14px'})
        $('.sub-div-table-tr').css({'height': '46px'})
        $('.sub-div-table-tr').css({'font-size': '14px'})
        $('.ant-checkbox-wrapper').css({'font-size': '14px'})
        if (flag) {
            this.updateLocalStorageData('size', 'large')
        }
    }

    small = (flag = false) => {
        $('.divTableRow').css({'height': '25px'})
        $('.divTablelistRow').css({'height': '25px'})
        $('.divTableCell').css({'font-size': '11px'})
        $('.sub-div-table-tr').css({'height': '25px'})
        $('.sub-div-table-tr').css({'font-size': '11px'})
        $('.ant-checkbox-wrapper').css({'font-size': '11px'})
        if (flag) {
            this.updateLocalStorageData('size', 'small')
        }
    }

    left = (flag = false) => {
        $('.divTableCell').css({'text-align': 'left'})
        if (flag) {
            this.updateLocalStorageData('align', 'l')
        }
    }

    right = (flag = false) => {
        $('.divTableCell').css({'text-align': 'right'})
        if (flag) {
            this.updateLocalStorageData('align', 'r')
        }
    }

    center = (flag = false) => {
        $('.divTableCell').css({'text-align': 'center'})
        if (flag) {
            this.updateLocalStorageData('align', 'm')
        }
    }

    exist = (flag = false) => {
        $('#paleTable').css({'background-color': 'white'})
        if (flag) {
            this.updateLocalStorageData('border', true)
        }
    }

    none = (flag = false) => {
        $('#paleTable').css({'background-color': ''})
        if (flag) {
            this.updateLocalStorageData('border', false)
        }
    }

    doubleChange = () => {
        this.setState({doubleShow: !this.state.doubleShow})
    }

    render() {
        const {isPagination, showSizeChanger} = this.props
        const {search, doubleShow, defautDoubleShow} = this.state
        const totals = search.totalCount
        const ButtonGroup = Button.Group
        return <Row className='paging-margin' style={{width: '100%'}}>
            <Col sm={2} style={{display: defautDoubleShow ? 'block' : 'none', padding: '10px 0'}}>
                <Button icon='double-right' onClick={this.doubleChange} type='dashed'
                        style={{display: !doubleShow ? 'block' : 'none'}} size='small'/>
                <ButtonGroup style={{display: doubleShow ? 'block' : 'none'}} size='small'>
                    <Button icon='double-left' onClick={this.doubleChange}/>
                    <Button icon='minus' onClick={() => this.small(true)}/>
                    <Button icon='plus' onClick={() => this.large(true)}/>
                    {/* <Button onClick={() => this.left(true)}>L</Button>
                    <Button onClick={() => this.center(true)}>M</Button>
                    <Button onClick={() => this.right(true)}>R</Button>
                   <Button icon='plus-square-o' onClick={() => this.exist(true)}/>
                    <Button icon='close-square-o' onClick={() => this.none(true)}/>*/}
                </ButtonGroup>
            </Col>
            <Col sm={10} style={{display: isPagination ? 'block' : 'none'}}>
                <Paging showItemsNumber={showSizeChanger} loadPageCallback={::this.loadPageCallback}
                        currentPage={search.page} pageSize={search.pageSize}
                        pageCallback={::this.pageCallback}
                        total={totals && totals > 0 ? totals : 0}/>
            </Col>
        </Row>
    }
}
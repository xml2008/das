/**
 * Created by liang.wang on 21/1/6.
 */
import React from 'react'
import {DataBaseBatchControl} from '../../../../../../controller/Index'
import ManageCloudPanle from '../../../base/ManageCloudPanle'
import CommonHeader from '../../../base/common/CommonHeader'
import DatabaseBatchHeader from './DatabaseBatchHeader'
import PageBase from '../../../../page/PageBase'
import EditDataCell from './cell/EditDataCell'
import {DataList} from '../../../../utils/Index'
import {DataUtil} from '../../../../utils/util/Index'
import {View} from 'ea-react-dm-v14'
import {Modal} from 'antd'

@View(DataBaseBatchControl)
export default class DatabaseBatchManage extends ManageCloudPanle {

    constructor(props, context) {
        super(props, context)
        this.props.initModelName(this)
        this.initValueLink()
        this.state = {
            disabled: true
        }
        this.loadList()
    }

    creatSearchParams = searchInfo => {
        return searchInfo
    }

    loadData = searchParams => {
        this.props.loadList(searchParams, this, data => {
            if (data.code === 200) {
                this.setState({disabled: data.msg.list.length === 0})
            }
        })
    }

    loadList = () => {
        const searchInfo = this.getValueToJson(this.searchInfo)
        this.loadData(this.creatSearchParams(searchInfo), this)
    }


    onChange = (v, key) => {
        const path = this.searchResultList + '.list'
        const list = this.getValueToJson(path)
        list.forEach(i => {
            i[key] = v
        })
        this.setValueToImmutable(path, list)
    }

    createTd = () => {
        return {
            db_address: (item, ele) => {
                return <EditDataCell _key={item.key} item={ele} value={ele[item.key]}
                                     onChange={v => this.onChange(v, 'db_address')}/>
            },
            db_port: (item, ele) => {
                return <EditDataCell _key={item.key} item={ele} value={ele[item.key]}
                                     onChange={v => this.onChange(v, 'db_port')}/>
            }
        }
    }

    updateBatch = () => {
        let list = this.getValueToJson(this.searchResultList + '.list')
        list = DataUtil.ObjUtils.filterWhereListByKeys(list, ['id', 'db_address', 'db_port'])
        window.console.log(list)
        this.props.updateBatch(list, this, data => {
            this.showRes(data, '批量更新')
        })
    }

    submit = () => {
        Modal.confirm({
            title: '操作确认',
            content: <h5>{'请确认当前页数据批量更新，并且不做物理库链接校验?'}</h5>,
            width: 600,
            onOk: () => {
                this.updateBatch()
            }
        })
    }

    render() {
        const {disabled} = this.state
        const {loading} = this.getValueToJson(this.states)
        const _props = this.initProps({loading})
        return <PageBase title='批量操作物理库' navigation='数据访问平台 / 批量操作物理库'
                         addButtonShow={false} zDepth={1}>
            <CommonHeader {..._props} message='数据查询' loadList={::this.loadList}>
                <DatabaseBatchHeader {..._props} loadList={::this.loadList} submit={::this.submit} disabled={disabled}>
                    <DataList {..._props}
                              doubleShow={true}
                              loadData={::this.loadData}
                              createTd={::this.createTd()}/>
                </DatabaseBatchHeader>
            </CommonHeader>
        </PageBase>
    }
}

/**
 * Created by liang.wang on 21/1/6.
 */
import React from 'react'
import {DataBaseQueryAppidsControl} from '../../../../../../controller/Index'
import QueryBaseManageCloudPanle from '../../base/QueryBaseManageCloudPanle'
import CommonHeader from '../../../base/common/CommonHeader'
import DatabasePageListAppidsHeader from './DatabasePageListAppidsHeader'
import {DataList} from '../../../../utils/Index'
import {View} from 'ea-react-dm-v14'

@View(DataBaseQueryAppidsControl)
export default class DatabasePageListAppidsManage extends QueryBaseManageCloudPanle {

    constructor(props, context) {
        super(props, context)
        this.props.initModelName(this)
        this.initValueLink()
        this.loadList()
    }

    loadData = searchParams => {
        if(!searchParams.data){
            return
        }
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

    render() {
        const {loading} = this.getValueToJson(this.states)
        const _props = this.initProps({loading})
        return <CommonHeader {..._props} message='数据查询' loadList={::this.loadList}>
                <DatabasePageListAppidsHeader {..._props} loadList={::this.loadList}>
                    <DataList {..._props}
                              doubleShow={true}
                              loadData={::this.loadData}/>
                </DatabasePageListAppidsHeader>
            </CommonHeader>
    }
}

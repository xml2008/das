/**
 * Created by liang.wang on 21/1/12.
 */
import React, {Component} from 'react'
import PageBase from '../../page/PageBase'
import {Tabs} from 'antd'
import DatabasePageListAppidsManage from './db/appids/DatabasePageListAppidsManage'
import DatabasePageListGroupManage from './db/group/DatabasePageListGroupManage'
import AppListByDbNameManage from './app/dbmane/AppListByDbNameManage'
import AppListByDbSetNameManage from './app/dbSetmane/AppListByDbSetNameManage'
import DbSetListManage from './dbset/DbSetListManage'
import DatabasePageListDbSetManage from './db/dbset/DatabasePageListDbSetManage'

export default class QueryIndex extends Component {

    constructor(props, context) {
        super(props, context)
    }

    render() {
        const TabPane = Tabs.TabPane
        return <PageBase title='' navigation='数据访问平台 / 复合查询'
                         addButtonShow={false} showDivider={false} zDepth={1}>
            <Tabs defaultActiveKey='1'>
                <TabPane tab='根据APPID查询物理库信息' key='1'>
                    <DatabasePageListAppidsManage/>
                </TabPane>
                <TabPane tab='根据物理库名查询关联项目' key='2'>
                    <AppListByDbNameManage/>
                </TabPane>
                <TabPane tab='根据项目组查询物理库信息' key='3'>
                    <DatabasePageListGroupManage/>
                </TabPane>
                <TabPane tab='根据物理库名查询逻辑库' key='4'>
                    <DbSetListManage/>
                </TabPane>
                <TabPane tab='根据逻辑库名查询关联项目' key='5'>
                    <AppListByDbSetNameManage/>
                </TabPane>
                <TabPane tab='根据逻辑库名查询物理库' key='6'>
                    <DatabasePageListDbSetManage/>
                </TabPane>
            </Tabs>
        </PageBase>
    }
}

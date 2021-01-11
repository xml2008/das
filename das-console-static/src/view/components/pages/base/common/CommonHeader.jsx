/**
 * Created by liang.wang on 20/4/12.
 */
import React from 'react'
import ManageCloudPanle from '../ManageCloudPanle'
import {Tabs} from 'antd'
import {envButtons} from '../../../../../model/base/BaseModel'
import {DataUtil, UserEnv} from '../../../utils/util/Index'
import {SpinPlus} from '../../../utils/Index'

export default class CommonHeader extends ManageCloudPanle {

    static defaultProps = {
        type: 1, //环境传参，2.自定义切换
        loading: false,
        isAlert: false,
        message: '',
        style: {paddingTop: 0, overflow: 'auto', minWidth: 1400},
        loadList: () => {
        }
    }

    constructor(props, context) {
        super(props, context)
        this.modelName = props.modelName
        this.initValueLink()
        this.state = {
            loading: props.loading,
            isAlert: props.isAlert
        }
    }

    componentWillReceiveProps(props) {
        const {loading} = props
        if (loading != this.state.loading) {
            this.setState({loading})
        }
    }

    onChange = id => {
        const key = DataUtil.ObjUtils.findWhere(envButtons, {id: parseInt(id)})
        UserEnv.setAppEnv(key.name)
        this.props.loadList()
    }

    render() {
        const {loading, isAlert} = this.state
        const {children, message, type, style} = this.props
        const TabPane = Tabs.TabPane
        if (type === 1) {
            return <SpinPlus message={message} description='没有查到相关消息' tip='数据加载中...'
                             isAlert={isAlert} loading={loading} size='large'>
                <div style={style}>
                    {children}
                </div>
            </SpinPlus>
        }
        return <SpinPlus message={message} description='没有查到相关消息' tip='数据加载中...'
                         isAlert={isAlert} loading={loading} size='large'>
            <Tabs type='card' onChange={this.onChange}>
                {
                    envButtons.map(i => {
                        return <TabPane tab={i.name} key={i.id}/>
                    })
                }
            </Tabs>
            {children}
        </SpinPlus>
    }
}

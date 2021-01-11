/**
 * Created by liang.wang on 21/1/6.
 */
import React from 'react'
import {DataUtil} from '../../../utils/util/Index'
import ManageCloudPanle from '../../base/ManageCloudPanle'

export default class QueryBaseManageCloudPanle extends ManageCloudPanle {

    constructor(props, context) {
        super(props, context)
    }

    creatSearchParams = searchInfo => {
        return searchInfo
    }

    search = _this => {
        _this.props.loadList()
    }

    onChangeCallBack = (v, _this) => {
        if (DataUtil.StringUtils.isEmpty(v)) {
            _this.setState({disabled: true})
        } else {
            _this.setState({disabled: false})
        }
    }

    render() {
        return <div/>
    }
}

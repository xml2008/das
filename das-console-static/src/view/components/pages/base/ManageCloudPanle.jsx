/**
 * Created by liang.wang on 18/9/14.
 */
import Component from '../../utils/base/ComponentAlert'
import {DataUtil, CoreUtil} from '../../utils/util/Index'
import {dataConfig, display} from '../../../../model/base/BaseModel'
import _ from 'underscore'

export default class ManageCloudPanle extends Component {

    constructor(props, context) {
        super(props, context)
        this.syncLink = ''  //同步
        this.checkLink = '' //校验
        this.cleanExceptKeys = []
        this.validates = [] //['validate']
        this.searchInfoSelectId = ''
        this.initValueLink()
    }

    initValueLink = () => {
        this.editorTitlte = '配置'
        this.objName = this.modelName + '.item'
        this.searchResultList = this.modelName + '.searchResultList'
        this.searchInfo = this.modelName + '.searchResultList.searchInfo'
        this.searchInfoData = this.modelName + '.searchResultList.searchInfo.data'
        this.columnInfo = this.modelName + '.columnInfo'
        this.states = this.modelName + '.states'
        this.editeVisible = this.states + '.editeVisible'
        this.checkVisible = this.states + '.checkVisible'
        this.displayItemsButtons = this.modelName + '.displayItems.buttons'
        this.validates = ['validate']
        this.validateMessage = {}
    }

    getValue = key => {
        return this.getValueToJson(this.modelName + '.' + key)
    }

    setValue = (key, val) => {
        return this.setValueToImmutable(this.modelName + '.' + key, val)
    }

    updateState = _visible => {
        setTimeout(() => {
            this.setState({confirmLoading: false})
            this.setValueByReducers(this.editeVisible, _visible)
        }, 1000)
    }
    itemCheck = () => {
        return true
    }

    addItemSuccessCallBack = () => {
        this.showSuccessMsg('添加成功')
    }
    updateItemSuccessCallBack = (object, states) => {
        this.showSuccessMsg(states.editerType === 1 ? '修改成功' : '删除成功')
    }
    beforAddItem = object => {
        return object
    }
    handleOk = () => {
        const object = this.getValueToJson(this.objName)
        if (!this.itemCheck(object)) {
            return
        }
        const states = this.getValueToJson(this.states)
        let visible = false
        this.setState({
            confirmLoading: true
        }, () => {
            try {
                if (states.editerType == 0) {
                    this.addItem(this.beforAddItem(object), this, rs => {
                        if (rs.code === dataConfig.sucCode) {
                            this.reload()
                            this.cleanObjName()
                            this.addItemSuccessCallBack(rs)
                        } else {
                            //_this.showErrorsNotification(rs.msg)
                            visible = true
                        }
                        this.updateState(visible)
                    })
                } else if (states.editerType > 0) {
                    this.updateItem(object, this, rs => {
                        if (rs.code === dataConfig.sucCode) {
                            this.reload()
                            this.cleanObjName()
                            this.updateItemSuccessCallBack(rs, states)
                        } else {
                            //_this.showErrorsNotification(rs.msg)
                            visible = true
                        }
                        this.updateState(visible)
                    })
                }
            } catch (e) {
                this.setState({confirmLoading: false})
            }
        })
    }

    reloadCallBack = () => {
    }
    reload = () => {
        setTimeout(() => {
            this.loadList && this.loadList(this.getValueToJson(this.searchInfo), this, null, this.loadListFiler)
            this.reloadCallBack()
        }, 500)
    }

    handleCancel = () => {
        CoreUtil.validate.cleanMsg(this)
        this.setState({visible: false})
        this.setValueByReducers(this.editeVisible, false)
    }

    cleanObjName = () => {
        let item = this.getValueToJson(this.objName)
        this.setValueToImmutable(this.objName, DataUtil.ObjUtils.cleanJson(item, this.cleanExceptKeys))
        CoreUtil.validate.cleanMsg(this)
    }

    /**
     * 子类可覆盖
     * @returns {{state: boolean, msg: string}}
     */
    addValidate = () => {
        return {
            state: true,
            msg: 'success'
        }
    }

    addCallBack = () => {
        this.cleanObjName()
    }
    add = event => {
        const rs = this.addValidate()
        if (!rs.state) {
            this.showErrorsMsg(rs.msg)
            return
        }
        this.addCallBack()
        const states = {
            editeVisible: true,
            editerType: 0,
            title: '新增' + this.editorTitlte
        }
        this.replaceValuesToItem(this.states, states)
        event.stopPropagation()
    }

    deleteCallBack = () => {
    }
    delete = obj => {
        this.deleteItem(obj, this, (_this, rs) => {
            if (rs.code === 200) {
                this.reload()
                this.showSuccessMsg('删除成功')
                this.deleteCallBack()
            } else {
                this.reload()
                this.showErrorsNotification(rs.msg)
            }
        })
    }

    editorCallBack = item => {
        return item
    }
    editorFiler = item => {
        return item
    }
    editor = item => {
        this.replaceValuesToItem(this.objName, this.editorFiler(item))
        this.editorCallBack(item)
        const states = {
            editeVisible: true,
            editerType: 1,
            title: '编辑' + this.editorTitlte
        }
        this.replaceValuesToItem(this.states, states)
    }

    /**
     * 表格默认第一行选中
     * @param data
     */
    loadListFilerBefore = data => {
        return data
    }
    loadListFiler = data => {
        data = this.loadListFilerBefore(data)
        if (data.list && data.list.length > 0) {
            data.list.map((item, i) => {
                if (i === 0) {
                    item.checkbox = true
                } else {
                    item.checkbox = false
                }
            })
            this.setState({currentCheckedId: data.list[0].id})
        } else if (data.list && data.list.length === 0) {
            this.setState({currentCheckedId: 0})
        }
    }

    sync = item => {
        if (this.syncLink && this.syncLink.length > 5) {
            item = {id: item.id}
            CoreUtil.fetch.fetchGet(this.syncLink, item, this, data => {
                if (data.code == 200) {
                    this.showSuccessMsg(this.configName + '同步数据成功！！')
                } else {
                    this.showErrorsNotification(this.configName + '同步数据失败！！可重试！！' + data.msg)
                }
            })
        }
    }

    check = item => {
        if (this.checkLink && this.checkLink.length > 5) {
            CoreUtil.fetch.fetchGet(this.checkLink, {id: item.id}, this, data => {
                if ((data.code == 500 && DataUtil.is.String(data.msg) && ((DataUtil.is.Array(data.item) && data.item.length === 0) || data.item == null)) || (!data.item.appId && !data.item.namespace && data.item.message)) {
                    this.showErrorsNotification(data.msg)
                } else if (DataUtil.is.Object(data.item)) {
                    const states = {
                        checkVisible: true,
                        checkData: data
                    }
                    this.replaceValuesToItem(this.states, states)
                }
            })
        }
    }

    handleCheckCancel = () => {
        this.setValueByReducers(this.checkVisible, false)
    }

    getDefaultSelectedCallBack = id => {
        return id
    }
    beforeDefaultSelected = item => {
        return item
    }
    getDefaultSelected = item => {
        if (_.isEmpty(item) || !this.searchInfoSelectId) {
            return
        }
        this.beforeDefaultSelected(item)
        this.setValueByReducers(this.searchInfoSelectId, item[0].id, this, () => {
            this.reload()
            this.getDefaultSelectedCallBack(item[0].id)
        }, 300)
    }

    dispalyManage = {
        initButtons: url => {
            url && CoreUtil.fetch.fetchGet(url, null, this, data => {
                if (data.code === 200) {
                    this.setValueToImmutable(this.displayItemsButtons, data.msg)
                }
            })
        },
        filter: (displaybuttons, _buttons, buttons) => {
            _buttons.forEach(e => {
                const key = display.buttons[e.type]
                if (displaybuttons[key]) {
                    buttons.push(e)
                }
            })
        }
    }

    visibleCallback = visible => {
        this.setValueByReducers(this.states + '.editeVisible', visible)
    }

    isAddItem = () => {
        const states = this.getValueToJson(this.states)
        return states.editerType == 0
    }

    initProps = (props = {}) => {
        const modelName = this.modelName
        const {setValueByReducers, getItem, postItem} = this.props
        props = Object.assign(props, {setValueByReducers, modelName, getItem, postItem})
        props.states = this.states
        props[this.modelName.toLocaleLowerCase()] = this.props[this.modelName.toLocaleLowerCase()]
        return props
    }

    initSearchResultList = (data, _this, isPagination = true, format = {
        list: 'list',
        totalCount: 'totalCount',
        pageSize: 'pageSize'
    }) => {
        const searchResultList = _this.getValueToJson(_this.searchResultList)
        searchResultList.list = CoreUtil.store.getJsonValeByKey(data, format.list)
        if (isPagination) {
            searchResultList.searchInfo.pageSize = CoreUtil.store.getJsonValeByKey(data, format.pageSize)
            searchResultList.searchInfo.totalCount = CoreUtil.store.getJsonValeByKey(data, format.totalCount)
        }
        _this.setValueToImmutable(_this.searchResultList, searchResultList)
    }

    toPageWithUrlParams = (route, ps = {}) => {
        const param = CoreUtil.UrlUtils.getUrls()
        _.extend(param, ps)
        window.location.href = '/#/' + route + CoreUtil.UrlUtils.initParams(param)
    }

    binLoding = () => {
        this.replaceValuesToItem(this.states, {loading: true})
    }

    endLoding = () => {
        const endLodingTimer = setTimeout(() => {
            this.replaceValuesToItem(this.states, {loading: false})
            window.clearTimeout(endLodingTimer)
        }, 200)
    }

    showRes = (data, title, reload = true, updateState = true) => {
        if (data.code === dataConfig.sucCode) {
            this.showSuccessMsg(title + '成功!!')
            reload && this.loadList()
            updateState && this.updateState(false)
        } else {
            this.showErrorsNotification(title + '失败!!' + data.msg)
            updateState && this.updateState(true)
        }
    }

    onSetValueValidate = (value, _this) => {
        const curValueLink = _this.props.valueLink
        this.setState({curValueLink, value})
    }

    validate = (params = {}) => {
        params = _.extend({path: 'validate', objName: this.objName, key: null}, params)
        const valLink = this.modelName + '.' + params.path
        const data = this.getValueToJson(valLink)
        let item, v, flag = true, objName
        if (_.isEmpty(data)) {
            return
        }

        if (!_.isEmpty(params.key)) {
            _.isObject(data) && Object.keys(data).forEach(name => {
                const obj = data[name]
                Object.keys(obj.m).forEach(k => {
                    if (params.key === name) {
                        if (obj.l) {
                            objName = this.modelName + '.' + obj.l
                        } else {
                            objName = params.objName
                        }
                        v = this.getValueToJson(objName + '.' + name)
                        if (!CoreUtil.validate[k](v)) {
                            obj.s = obj.m[k]
                            this.showErrorsMsg(obj.s)
                            flag = false
                        } else {
                            obj.s = ''
                        }
                    }
                })
            })
            this.setValueToImmutable(valLink, data)
            return flag
        }

        for (let i in data) {
            item = data[i]
            if (item.l) {
                objName = this.modelName + '.' + item.l
            } else {
                objName = params.objName
            }
            v = this.getValueToJson(objName + '.' + i)
            for (let k in item.m) {
                if (!CoreUtil.validate[k](v)) {
                    item.s = item.m[k]
                    this.showErrorsMsg(item.m[k])
                    flag = false
                    //break
                } else {
                    item.s = ''
                }
            }
            if (!flag) {
                //break
            }
        }

        if (!flag) {
            this.setValueToImmutable(valLink, data)
        }
        return flag
    }

    initvalidateMessage = {
        set: (key, title) => {
            this.validateMessage[key] = title
        },
        init: (params = {}) => {
            params = _.extend({path: 'validate'}, params)
            const valLink = this.modelName + '.' + params.path
            const data = this.getValueToJson(valLink)
            const validateMessage = this.validateMessage
            _.isObject(data) && Object.keys(data).forEach(name => {
                const obj = data[name]
                Object.keys(obj.m).forEach(k => {
                    if (_.isEmpty(obj.m[k])) {
                        obj.m[k] = validateMessage[name]
                    }
                })
            })
            this.setValueToImmutable(valLink, data)
        }
    }
}

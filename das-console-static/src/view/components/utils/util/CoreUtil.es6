import React from 'react'
import {DataUtil, UserEnv} from './Index'
import {fetch} from 'ea-react-dm-v14'
import _ from 'underscore'
import $ from 'jquery'
import {Tag} from 'antd'
import {dataConfig} from '../../../../model/base/BaseModel'

let CoreUtil = CoreUtil || {}

CoreUtil.store = {
    getValueByReducers: function (props, valueLink) {
        (!props || !valueLink) && window.console.error('CoreUtil.store.getValueByReducers : props or valueLink is undefined', props, valueLink)
        let keys = valueLink.split('.')
        const modelName = keys.shift()
        const model = props[modelName.toLowerCase()]
        if (!model || !keys) {
            window.console.warn('CoreUtil.store.getValueByReducers model or keys miss:', props, valueLink)
            return ''
        }
        let val = '', rs = []
        try {
            if (keys.length > 1) {
                for (var i in keys) {
                    if (i == 0) {
                        rs = model.get(keys[i])
                    } else if (i > 0) {
                        rs = rs.get(keys[i])
                    }
                }
                val = rs
            } else {
                val = model.get(keys[0])
            }
        } catch (e) {
            window.console && window.console.warn('CoreUtil.store.getValueByReducers', keys.join(), rs, e)
        }
        return val
    },
    getValeByKey: function (item, valueLink) {
        if (valueLink.includes('.')) {
            let keys = valueLink.split('.')
            let val = '', rs = []
            try {
                if (keys.length > 1) {
                    for (var i in keys) {
                        if (i == 0) {
                            rs = item.get(keys[i])
                        } else if (i > 0) {
                            rs = rs.get(keys[i])
                        }
                    }
                    val = rs
                } else {
                    val = item.get(keys[0])
                }
            } catch (e) {
                window.console && window.console.warn('CoreUtil.store.getValueByReducers', keys.join(), rs, e)
            }
            return val
        }
        return item.get(valueLink)
    },
    getJsonValeByKey: function (item, valueLink) {
        if (valueLink.includes('.')) {
            let keys = valueLink.split('.')
            let val = '', rs = []
            try {
                if (keys.length > 1) {
                    for (var i in keys) {
                        if (i == 0) {
                            rs = item[keys[i]]
                        } else if (i > 0) {
                            rs = rs[keys[i]]
                        }
                    }
                    val = rs
                } else {
                    val = item[keys[0]]
                }
            } catch (e) {
                window.console && window.console.warn('CoreUtil.store.getValueByReducers', keys.join(), rs, e)
            }
            return val
        }
        return item[valueLink]
    }
}

CoreUtil.UrlUtils = {
    /**
     * 获取get请求所有参数
     * 例http://a.html?b=1&c=2
     * @returns {b:1,c:2}
     */
    getUrls: function () {
        var aQuery = window.location.href.split('?')
        var aGET = {}
        if (aQuery.length > 1) {
            var aBuf = aQuery[1].split('&')
            for (var i = 0, iLoop = aBuf.length; i < iLoop; i++) {
                var aTmp = aBuf[i].split('=')
                aGET[aTmp[0]] = aTmp[1]
            }
        }
        return aGET
    },
    /**
     * 组合请求参数
     * @param {b:1,c:2}
     * @returns ?b=1&c=2
     */
    initParams: function (data) {
        if (!data || _.isEmpty(data)) {
            return ''
        }
        var arr = []
        for (var item in data) {
            arr.push('&' + item + '=')
            arr.push(data[item])
        }
        if (arr.length == 0) {
            return ''
        }
        var str = arr.join('')
        return '?' + str.substring(1, str.length)
    }
}

CoreUtil.fetch = {
    fetchGet: (url, _this, param = {}, callBack) => {
        const arges = _.extend({data: {}, isMock: UserEnv.isMock(), param: null}, param)
        let _data = {
            method: 'GET',
            mode: 'cors',
            timeout: 60000,
            headers: {'Content-Type': 'application/json', 'Authorization': '123213213'}
        }
        _data = _.extend(_data, arges.data)
        let args = ''
        const params = CoreUtil.UrlUtils.initParams(arges.param)
        if (url.includes('?')) {
            const arr = url.split('?')
            if (!_.isEmpty(arr)) {
                url = arr[0]
                if (arr.length > 2) {
                    arr.forEach((e, i) => {
                        if (!params && i === 1) {
                            args += '?' + arr[i]
                        } else if (i > 1) {
                            args += '&' + arr[i]
                        }
                    })
                    url += params + args
                } else if (arr.length === 2) {
                    if (!params) {
                        args = '?' + arr[1]
                    } else {
                        args = '&' + arr[1]
                    }

                }
            }
        }
        url += params + args
        if (arges.isMock) {
            fetch(url, _data).then(data => {
                    _this && callBack && callBack(data, _this)
                }, (error) => {
                    window.console.error('fetchGet error : ' + url, error)
                }
            )
        } else {
            window.fetch(url, _data).then(res => {
                return res.json()
            }).then(data => {
                _this && callBack && callBack(data, _this)
                return data
            }).catch(err => {
                window.console.log('请求错误', err)
            })
        }
        return () => {
            return null
        }
    },
    fetchPost: (url, _this, param = {}, callBack) => {
        const arges = _.extend({
            data: {}, isMock: UserEnv.isMock(), errCallBack: null, param: null
        }, param)

        const _data = _.extend({
            body: JSON.stringify(arges.param), mode: 'cors', method: 'POST',
            timeout: 60000, headers: {'Content-Type': 'application/json;charset=UTF-8', 'Authorization': '123213213'}
        }, arges.data)

        if (arges.isMock) {
            fetch(url, _data).then(data => {
                    _this && callBack && callBack(data, _this)
                }, (error) => {
                    _this && arges.errCallBack && arges.errCallBack(error, _this)
                    window.console.error('fetchPost error : ' + url, error)
                }
            )
        } else {
            window.fetch(url, _data).then(res => {
                return res.json()
            }).then(data => {
                _this && callBack && callBack(data, _this)
                return data
            }).catch(err => {
                window.console.error('fetchPost error : ', url, err)
            })
        }
        return () => {
            return null
        }
    }
}

CoreUtil.load = {
    getList: (url, param, _this, key) => {
        CoreUtil.fetch.fetchGet(url, param, _this, (data, _this) => {
                if (data.code === dataConfig.sucCode) {
                    if (!DataUtil.ObjUtils.isEqual(_this.state[key], data.msg)) {
                        _this.state[key] = data.msg
                        _this.setState(_this.state)
                    }
                } else if (data.code === dataConfig.errCode) {
                    _this.showErrorsNotification(data.msg)
                }
            }
        )
    }
}

CoreUtil.setValueToImmutable = (data, _this, param = {}, callback) => {
    const params = _.extend({dataPath: 'msg', msg: 'msg', path: '', isSetVal: true}, param)
    if (data.code === dataConfig.sucCode) {
        params.isSetVal && _this.setValueToImmutable(params.path, CoreUtil.store.getJsonValeByKey(data, params.dataPath))
    } else {
        _this.showErrorsNotification(data[params.msg])
    }
    callback && callback(data, _this)
}

CoreUtil.setSearchResultList = (data, _this, param = {}) => {
    const params = _.extend({
        msg: 'msg', isPagination: true, callback: null, message: 'detailMessage',
        list: 'msg.list', totalCount: 'msg.totalCount', pageSize: 'msg.pageSize'
    }, param)
    const initSearchResultList = (data, _this, isPagination = true, format) => {
        const searchResultList = _this.getValueToJson(_this.searchResultList)
        searchResultList.list = CoreUtil.store.getJsonValeByKey(data, format.list)
        if (isPagination) {
            searchResultList.searchInfo.totalCount = CoreUtil.store.getJsonValeByKey(data, format.totalCount)
            const pageSize = CoreUtil.store.getJsonValeByKey(data, format.pageSize)
            if (format.pageSize && pageSize) {
                searchResultList.searchInfo.pageSize = pageSize
            }
        }
        _this.setValueToImmutable(_this.searchResultList, searchResultList)
    }
    if (data.code === dataConfig.sucCode) {
        if ((params.isPagination && CoreUtil.store.getJsonValeByKey(data, params.totalCount) == 0) || _.isEmpty(CoreUtil.store.getJsonValeByKey(data, params.list))) {
            _this.replaceValuesToItem(_this.states, {isAlert: true})
        } else {
            _this.replaceValuesToItem(_this.states, {isAlert: false})
        }
        initSearchResultList(data, _this, params.isPagination, {
            list: params.list,
            totalCount: params.totalCount,
            pageSize: params.pageSize
        })
        _this.endLoding()
    } else {
        _this.showErrorsNotification(data[params.message])
        setTimeout(() => {
            _this.endLoding()
        }, 5000)
    }
    params.callback && params.callback(data, _this)
}


CoreUtil.createContent = data => {
    if (!DataUtil.is.String(data)) {
        return data
    }
    const content = []
    if (data.includes(';')) {
        const arr = data.split(';')
        arr.forEach(item => {
            content.push(<p>{item}</p>)
        })
        return <div>
            {content}
        </div>
    }
    if (data.includes(',')) {
        const arr = data.split(',')
        let names = [], n = 0
        arr.forEach((item, i) => {
            n = i + 1
            names.push(item)
            if (n % 4 === 0) {
                content.push(<p>{names.join(',')}</p>)
                names = []
            }
        })
        if (names.length > 0) {
            content.push(<p>{names.join(',')}</p>)
        }
    } else {
        return <div>
            {data}
        </div>
    }
    return <div>
        {content}
    </div>
}


/**
 * table 弹出详细tags
 */
CoreUtil.createTaqgs = (item, arr) => {
    let rs = [], row = [], style = {padding: '8px'}
    arr.forEach((ele, i) => {
        const n = i + 1
        const tag = <Tag color={item.popoveTags.color}>{ele}</Tag>
        row.push(tag)
        if (i != 0 && n % item.popoveTags.rowSize === 0) {
            rs.push(<div style={style}>{row}</div>)
            row = []
        }
    })
    if (rs.length === 0 && row.length > 0) {
        return <div style={style}>{row}</div>
    }
    return rs
}


CoreUtil.validate = {
    zero: str => {
        if (DataUtil.StringUtils.trim(str + '') == '0') {
            return false
        }
        return true
    },
    empty: obj => {
        if (_.isObject(obj) && _.isEmpty(obj)) {
            return false
        } else {
            return CoreUtil.validate.required(obj)
        }
    },
    required: str => {
        if (str == null || str == undefined || DataUtil.StringUtils.trim(str + '') == '') {
            return false
        }
        return true
    },
    boolean: function (str) {
        return DataUtil.is.Boolean(str)
    },
    /**
     * 身份证校验
     * @param str
     * @returns {boolean}
     */
    lgalIdCard: str => {
        if (str == undefined) {
            return false
        }
        var idCardReg_15 = /^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$/
        //^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$
        var idCardReg_18 = /^(^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[Xx])$)$/

        return idCardReg_15.test($.trim(str.toLowerCase())) || idCardReg_18.test($.trim(str.toLowerCase()))
    },
    email: function (str) {
        var reg = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/
        return reg.test(str)
    },
    /**
     * 手机号
     * @param str 仅校验11位
     * @returns {boolean}
     */
    mobile: str => {
        str = str + ''
        if (str && str.length == 11) {
            return true
        }
        return false
        /*var reg = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/;
         return reg.test(str);*/
    },
    cleanMsg: _this => {
        !_.isEmpty(_this.validates) && _this.validates.forEach(name => {
            const vl = _this.modelName + '.' + name
            const validate = _this.getValueToJson(vl)
            !_.isEmpty(validate) && Object.keys(validate).forEach(i => {
                validate[i].s = undefined
            })
            _this.setValueToImmutable(vl, validate)
        })
    }/*,
     maxLength: function (str) {
     str = trim(str + '');
     if(str && str.length )
     return reg.test(str);
     }*/

}


export default CoreUtil
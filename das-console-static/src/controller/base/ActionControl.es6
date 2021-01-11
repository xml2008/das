import {Action} from 'ea-react-dm-v14'
import {CoreUtil, DataUtil} from '../../view/components/utils/util/Index'
import {urls} from '../../model/base/BaseModel'
import _ from 'underscore'

export default class ActionControl extends Action {

    static initModelName(_this) {
        _this.modelName = this.getModelName().replace(/\$/g, '')
        return () => {
            return null
        }
    }

    static loadSetSearchResultList(type, param, _this, callback) {
        _this.binLoding()
        return CoreUtil.fetch.fetchGet(urls.getUrl(type), _this, {param}, (data, _this) => {
            CoreUtil.setSearchResultList(data, _this, {callback})
        })
    }

    static loadPostSetSearchResultList(type, param, _this, callback) {
        _this.binLoding()
        return CoreUtil.fetch.fetchPost(urls.getUrl(type), _this, {param}, (data, _this) => {
            CoreUtil.setSearchResultList(data, _this, {callback})
        })
    }

    static getItem(type, param, _this, path, callback) {
        return CoreUtil.fetch.fetchGet(urls.getUrl(type), _this, {param}, (data, _this) => {
            if (DataUtil.is.String(path)) {
                CoreUtil.setValueToImmutable(data, _this, {path: _this.modelName + '.' + path}, callback)
            } else if (_.isFunction(path)) {
                CoreUtil.setValueToImmutable(data, _this, {isSetVal: false}, path)
            }
        })
    }

    static postItem(type, param, _this, path, callback) {
        return CoreUtil.fetch.fetchPost(urls.getUrl(type), _this, {param}, (data, _this) => {
            if (DataUtil.is.String(path)) {
                CoreUtil.setValueToImmutable(data, _this, {path: _this.modelName + '.' + path}, callback)
            } else if (_.isFunction(path)) {
                CoreUtil.setValueToImmutable(data, _this, {isSetVal: false}, path)
            }
        })
    }

    static updateItem(type, param, _this, path, callback) {
        return CoreUtil.fetch.fetchPost(urls.getUrl(type), _this, param, (data, _this) => {
            if (DataUtil.is.String(path)) {
                CoreUtil.setValueToImmutable(data, _this, {path: _this.modelName + '.' + path}, callback)
            } else if (_.isFunction(path)) {
                CoreUtil.setValueToImmutable(data, _this, {isSetVal: false}, path)
            }
        })
    }
}
import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {AppListByDbSetNameModel} from '../../../model/Index'

@Control(AppListByDbSetNameModel)
export default class AppListByDbSetNameControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.app_page_by_dbset_names, param, _this, callback)
    }

}
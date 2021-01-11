import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {AppListByDbNameModel} from '../../../model/Index'

@Control(AppListByDbNameModel)
export default class AppListByDbNameControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.app_page_by_db_names, param, _this, callback)
    }

}
import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {DataBaseQueryDbSetModel} from '../../../model/Index'

@Control(DataBaseQueryDbSetModel)
export default class DataBaseQueryDbSetControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.db_page_by_dbset_names, param, _this, callback)
    }

}
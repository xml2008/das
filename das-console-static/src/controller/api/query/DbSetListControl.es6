import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {DbSetListModel} from '../../../model/Index'

@Control(DbSetListModel)
export default class DbSetListControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.dbset_page_by_db_names, param, _this, callback)
    }

}
import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {DataBaseQueryGroupModel} from '../../../model/Index'

@Control(DataBaseQueryGroupModel)
export default class DataBaseQueryGroupControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.db_page_by_group_ids, param, _this, callback)
    }

}
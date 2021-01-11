import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {DataBaseQueryAppidsModel} from '../../../model/Index'

@Control(DataBaseQueryAppidsModel)
export default class DataBaseQueryAppidsControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.db_page_by_appids, param, _this, callback)
    }

}
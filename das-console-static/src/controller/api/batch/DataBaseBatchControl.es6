import {Control} from 'ea-react-dm-v14'
import {urls} from '../../../model/base/BaseModel'
import ActionControl from '../../base/ActionControl'
import {DatabaseBatchModel} from '../../../model/Index'

@Control(DatabaseBatchModel)
export default class DataBaseBatchControl extends ActionControl {

    static loadList(param, _this, callback) {
        return this.loadPostSetSearchResultList(urls.type.db_page_list, param, _this, callback)
    }

    static updateBatch(param, _this, callback) {
        param = {param, data: {method: 'PUT'}}
        return this.updateItem(urls.type.db_update_batch, param, _this, callback)
    }

}
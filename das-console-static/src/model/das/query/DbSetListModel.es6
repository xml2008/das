import {Model} from 'ea-react-dm-v14'

@Model('DbSetListModel')
export default class DbSetListModel {

    static rs

    static states = {
        item: {},
        title: '',
        isAlert: false,
        loading: false,
        editeVisible: false
    }

    static searchResultList = {
        list: [],
        searchInfo: {
            totalCount: 0,
            pageSize: 10,
            page: 1,
            data: ''
        }
    }


    static columnInfo = {
        column: [
            {
                name: '所属组',
                width: 10,
                key: 'group_name'
            },
            {
                name: '逻辑库名',
                width: 20,
                key: 'name'
            },
            {
                name: '物理库名',
                width: 20,
                key: 'db_catalog'
            },
            {
                name: 'APPID',
                width: 50,
                key: 'app_id'
            }
        ]
    }

}
import {Model} from 'ea-react-dm-v14'

@Model('DataBaseQueryDbSetModel')
export default class DataBaseQueryDbSetModel {

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
                name: '类型',
                width: 15,
                map: {1: 'MySql', 2: 'SQLServer'},
                key: 'db_type'
            },
            {
                name: '所属组',
                width: 15,
                key: 'group_name'
            },
            {
                name: '数据库标识符',
                width: 20,
                key: 'db_name'
            },
            {
                name: '物理库名',
                width: 20,
                key: 'db_catalog'
            },
            {
                name: '地址',
                width: 15,
                key: 'db_address'
            },
            {
                name: '端口',
                width: 15,
                key: 'db_port'
            }
        ]
    }

}
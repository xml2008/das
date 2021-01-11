import {Model} from 'ea-react-dm-v14'

@Model('AppListByDbSetNameModel')
export default class AppListByDbSetNameModel {

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
                name: 'APPID',
                width: 10,
                key: 'app_id'
            },
            {
                name: '项目名',
                width: 20,
                key: 'name'
            },
            {
                name: '负责人',
                width: 20,
                key: 'user_name'
            },
            {
                name: '备注(项目中文名，等其他描述)',
                width: 40,
                key: 'comment'
            }
        ]
    }

}
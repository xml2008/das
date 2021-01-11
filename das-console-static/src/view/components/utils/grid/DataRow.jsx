/**
 * Created by liang.wang on 17/9/7.
 */
import React, {Component} from 'react'
import './DataRow.less'
import QueueAnim from 'rc-queue-anim'
import {DataUtil} from '../util/Index'

export default class DataRow extends Component {

    static defaultProps = {
        createTd: (item, ele, index) => {
            window.console.log(item, ele, index)
        },
        createTdButton: (ele, row) => {
            window.console.log(ele, row)
        }
    }

    constructor(props, context) {
        super(props, context)
        this.state = {
            content: '',
            list: props.list,
            columnInfo: props.columnInfo,
        }
    }

    componentWillReceiveProps(nextProps) {
        const {list, columnInfo} = nextProps
        if (!DataUtil.ObjUtils.isEqual(list, this.state.list) || !DataUtil.ObjUtils.isEqual(columnInfo, this.props.columnInfo)) {
            this.setState({list, columnInfo})
        }
    }

    createTd = (item, ele, index) => {
        return this.props.createTd(item, ele, index)
    }

    render() {
        let {columnInfo, list} = this.state
        const {search, createTdButton} = this.props
        let index = (search.page - 1) * search.pageSize
        let rowNo = 1
        return (
            <div className='tradeRow'>
                <div className='divTable paleTable' id='paleTable'>
                    <QueueAnim className='divTableBody' duration={300} component='div' interval={10}
                               type={['scaleX', 'right']} delay={10}>
                        {
                            list && list.map(ele => {
                                    rowNo++
                                    index += 1
                                    return <div key={rowNo} className='divTableRow'>
                                        {
                                            columnInfo.column && columnInfo.column.map((item, i) => {
                                                if (item.button) {
                                                    return <div style={{
                                                        width: item.width + '%',
                                                        paddingTop: '0px',
                                                        paddingBottom: '0px',
                                                        color: 'rgba(0,0,0,.85)'
                                                    }} className='divTableCell' key={i}>{createTdButton(item, ele, index)}
                                                    </div>
                                                }
                                                return <div className='divTableCell' key={i}
                                                            style={{width: item.width + '%', color: 'rgba(0,0,0,.85)',}}>
                                                    {::this.createTd(item, ele, index)}
                                                </div>
                                            })
                                        }
                                    </div>
                                }
                            )
                        }
                    </QueueAnim>
                </div>
            </div>
        )
    }
}
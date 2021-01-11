/**
 * Created by liang.wang on 19/12/12.
 */
import React from 'react'
import {Col, Row, Button} from 'antd'
import {Itemlabel, InputAntPlus} from '../../../../utils/Index'
import {DataUtil} from '../../../../utils/util/Index'
import ManageCloudPanle from '../../../base/ManageCloudPanle'

export default class DatabaseBatchHeader extends ManageCloudPanle {

    static defaultProps = {
        disabled: true
    }

    constructor(props, context) {
        super(props, context)
        this.modelName = props.modelName
        this.initValueLink()
        this.topicNames = {
            labelCol: {
                sm: {span: 4}
            },
            wrapperCol: {
                sm: {span: 20}
            }
        }
        this.state = {
            disabled: props.disabled,
            values: []
        }
    }

    componentWillReceiveProps(nextProps) {
        const {disabled} = nextProps
        if (disabled != this.state.disabled) {
            this.setState({disabled})
        }
    }

    search = () => {
        this.props.loadList()
    }

    clearSearch = () => {
        let data = this.getValueToJson(this.searchInfoData)
        data = DataUtil.ObjUtils.cleanJson(data)
        this.setValueToImmutable(this.searchInfoData, data)
    }

    render() {
        const {disabled} = this.state
        const _props = this.initProps()
        const {children} = this.props
        const data = this.getValueToJson(this.searchInfoData)
        return <div style={{padding: '10px 0px 0px 0px'}}>
            <Row style={{padding: '0px 0px 10px 0px'}}>
                <Col sm={6} style={{fontSize: '17px', fontWeight: 'bolder'}}>
                    <Itemlabel title='数据库名: ' type={2} formLayout={this.topicNames} style={{marginBottom: '0px'}}>
                        <InputAntPlus {..._props} valueLink={this.searchInfoData + '.db_catalog'}
                                      value={data.db_catalog}/>
                    </Itemlabel>
                </Col>
                <Col sm={6} style={{fontSize: '17px', fontWeight: 'bolder'}}>
                    <Itemlabel title='地址: ' type={2} formLayout={this.topicNames} style={{marginBottom: '0px'}}>
                        <InputAntPlus {..._props} valueLink={this.searchInfoData + '.db_address'}
                                      value={data.db_address}/>
                    </Itemlabel>
                </Col>
                <Col sm={5} style={{fontSize: '17px', fontWeight: 'bolder'}}>
                    <Itemlabel title='端口: ' type={2} formLayout={this.topicNames} style={{marginBottom: '0px'}}>
                        <InputAntPlus {..._props} valueLink={this.searchInfoData + '.db_port'}
                                      value={data.db_port}/>
                    </Itemlabel>
                </Col>
                <Col sm={2} style={{display: 'grid', paddingLeft: '10px'}}>
                    <Button icon='search' type='primary' size='large' onClick={this.search}>查询</Button>
                </Col>
                <Col sm={2} style={{display: 'grid', paddingLeft: '10px'}}>
                    <Button icon='reload' size='large' onClick={::this.clearSearch}>条件重置</Button>
                </Col>
                <Col sm={3} style={{display: 'grid', paddingLeft: '10px'}}>
                    <Button icon='save' type='primary' size='large' disabled={disabled}
                            onClick={this.props.submit}>提交</Button>
                </Col>
            </Row>
            <Row>
                <Col sm={24} style={{display: 'grid'}}>
                    {children}
                </Col>
            </Row>
        </div>
    }
}

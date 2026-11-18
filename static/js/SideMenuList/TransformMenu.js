import React, { Component } from 'react';
import { Button, ListGroup, ListGroupItem, Row, Col } from 'reactstrap';

import Slider from 'rc-slider';
import PoserManager from '../PoserManager';
// import 'rc-slider/assets/index.css';

import { Trans } from '@lingui/macro';

var D = 5.0;  // translate range
var S = 0.9;  // scale range


class TranslateSubMenu extends Component {
  constructor(props) {
    super(props);

    // let p = this.state.originPos = PoserManager.getObjectPos();
    // this.state.minPos = [p[0] - D, p[1] - D, p[2] - D];
    // this.state.maxPos = [p[0] + D, p[1] + D, p[2] + D];

    let p = PoserManager.getObjectPos()
    this.state = {
      originPos: p,
      minPos: [p[0] - D, p[1] - D, p[2] - D],
      maxPos: [p[0] + D, p[1] + D, p[2] + D]
    };

    this.translateOnChangeX = this.translateOnChangeX.bind(this);
    this.translateOnChangeY = this.translateOnChangeY.bind(this);
    this.translateOnChangeZ = this.translateOnChangeZ.bind(this);
    this.translateAfterChange = this.translateAfterChange.bind(this);

  }

  translateOnChangeX(v) {
    let p = this.state.originPos;
    PoserManager.setObjectPos(v, p[1], p[2]);
    this.setState({ originPos: [v, p[1], p[2]] });
  }

  translateOnChangeY(v) {
    let p = this.state.originPos;
    PoserManager.setObjectPos(p[0], v, p[2]);
    this.setState({ originPos: [p[0], v, p[2]] });
  }

  translateOnChangeZ(v) {
    let p = this.state.originPos;
    PoserManager.setObjectPos(p[0], p[1], v);
    this.setState({ originPos: [p[0], p[1], v] });
  }

  translateAfterChange() {
    let p = PoserManager.getObjectPos();
    this.setState({
      originPos: p,
      minPos: [p[0] - D, p[1] - D, p[2] - D],
      maxPos: [p[0] + D, p[1] + D, p[2] + D]
    });
  }

  render() {
    // console.log('transform render');
    return (
      <div>
        <Button block><Trans>Move</Trans></Button>
        <ListGroup>
          <ListGroupItem>
            <Row>
              <Col xs='1'>X</Col>
              <Col><Slider
                min={this.state.minPos[0]}
                max={this.state.maxPos[0]}
                defaultValue={this.state.originPos[0]}
                step={0.01}
                onChange={this.translateOnChangeX}
                onAfterChange={this.translateAfterChange}
              /></Col>
            </Row>
            <Row>
              <Col xs='1'>Y</Col>
              <Col><Slider
                min={this.state.minPos[1]}
                max={this.state.maxPos[1]}
                defaultValue={this.state.originPos[1]}
                step={0.01}
                onChange={this.translateOnChangeY}
                onAfterChange={this.translateAfterChange}
              /></Col>
            </Row>
            <Row>
              <Col xs='1'>Z</Col>
              <Col><Slider
                min={this.state.minPos[2]}
                max={this.state.maxPos[2]}
                defaultValue={this.state.originPos[2]}
                step={0.01}
                onChange={this.translateOnChangeZ}
                onAfterChange={this.translateAfterChange}
              /></Col>
            </Row>
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}

export class TransformModelMenu extends Component {
  constructor(props) {
    super(props);

    let s = PoserManager.getScale();
    this.state = {
      originScale: s,
      minScale: Math.max(0.0, s - S),
      maxScale: s + S
    };

    this.scaleAfterChangeProportional = this.scaleAfterChangeProportional.bind(this);
    // this.forceUpdate();
  }

  scaleAfterChangeProportional() {
    let s = PoserManager.getScale();
    this.setState({
      originScale: s,
      minScale: Math.max(0.0, s - S),
      maxScale: s + S
    });
  }

  render() {
    // console.log('transform render');
    return (
      <div className="side-menu">
        <TranslateSubMenu />

        <Button block><Trans>Scale</Trans></Button>

        <ListGroup>
          <ListGroupItem>
            <Row>
              <Col xs='1'>S</Col>
              <Col>
                <Slider
                  min={this.state.minScale}
                  max={this.state.maxScale}
                  defaultValue={this.state.originScale}
                  step={0.01}
                  onChange={PoserManager.setScale}
                  onAfterChange={this.scaleAfterChangeProportional} /></Col>
            </Row>
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}


export class TransformPropMenu extends Component {
  constructor(props) {
    super(props);

    let s = PoserManager.getPropScale();
    this.state = {
      originScaleProp: s,
      minScaleProp: [Math.max(0.0, s[0] - S), Math.max(0.0, s[1] - S), Math.max(0.0, s[2] - S)],
      maxScaleProp: [s[0] + S, s[1] + S, s[2] + S]
    };

    this.scaleOnChangeX = this.scaleOnChangeX.bind(this);
    this.scaleOnChangeY = this.scaleOnChangeY.bind(this);
    this.scaleOnChangeZ = this.scaleOnChangeZ.bind(this);
    this.scaleAfterChange = this.scaleAfterChange.bind(this);
    // this.forceUpdate();
  }

  scaleOnChangeX(v) {
    let s = this.state.originScaleProp;
    PoserManager.setPropScale(v, s[1], s[2]);
    this.setState({ originScaleProp: [v, s[1], s[2]] });
  }

  scaleOnChangeY(v) {
    let s = this.state.originScaleProp;
    PoserManager.setPropScale(s[0], v, s[2]);
    this.setState({ originScaleProp: [s[0], v, s[2]] });
  }

  scaleOnChangeZ(v) {
    let s = this.state.originScaleProp;
    PoserManager.setPropScale(s[0], s[1], v);
    this.setState({ originScaleProp: [s[0], s[1], v] });
  }

  scaleAfterChange() {
    let s = PoserManager.getPropScale();
    this.setState({
      originScaleProp: s,
      minScaleProp: [Math.max(0.0, s[0] - S), Math.max(0.0, s[1] - S), Math.max(0.0, s[2] - S)],
      maxScaleProp: [s[0] + S, s[1] + S, s[2] + S]
    });
  }


  render() {
    // console.log('transform render');
    return (
      <div className="side-menu">
        <TranslateSubMenu />

        <Button block><Trans>Scale</Trans></Button>

        <ListGroup>
          <ListGroupItem>
            <Row>
              <Col xs='1'>X</Col>
              <Col><Slider
                min={this.state.minScaleProp[0]}
                max={this.state.maxScaleProp[0]}
                defaultValue={this.state.originScaleProp[0]}
                step={0.01}
                onChange={this.scaleOnChangeX}
                onAfterChange={this.scaleAfterChange}
              /></Col>
            </Row>
            <Row>
              <Col xs='1'>Y</Col>
              <Col><Slider
                min={this.state.minScaleProp[1]}
                max={this.state.maxScaleProp[1]}
                defaultValue={this.state.originScaleProp[1]}
                step={0.01}
                onChange={this.scaleOnChangeY}
                onAfterChange={this.scaleAfterChange}
              /></Col>
            </Row>
            <Row>
              <Col xs='1'>Z</Col>
              <Col><Slider
                min={this.state.minScaleProp[2]}
                max={this.state.maxScaleProp[2]}
                defaultValue={this.state.originScaleProp[2]}
                step={0.01}
                onChange={this.scaleOnChangeZ}
                onAfterChange={this.scaleAfterChange}
              /></Col>
            </Row>
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}
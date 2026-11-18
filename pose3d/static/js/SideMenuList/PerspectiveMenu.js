import React, { Component } from 'react';
import { Button, ListGroup, ListGroupItem, Row, Col } from 'reactstrap';
import PoserManager from '../PoserManager';

import Slider from 'rc-slider';

import { Trans } from '@lingui/macro';

export default class PerspectiveMenu extends Component {
  constructor(props) {
    super(props);

    this.originalFOV = PoserManager.getPerspective();
  }
  render() {
    return (
      <div className="side-menu">
        <Button block onClick={this.props.togglePerspectiveMenu}><Trans>Perspective Fov</Trans></Button>
        <ListGroup>
          <ListGroupItem>
            <Row>
              <Col xs='2'><Trans>Fov</Trans></Col>
              <Col><Slider min={30} max={90} defaultValue={this.originalFOV}
                onChange={PoserManager.setPerspective} 
                />
                </Col>
            </Row>
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}
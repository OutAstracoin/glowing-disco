import React, { Component } from 'react';
import { Button, ListGroup, ListGroupItem } from 'reactstrap';

import Slider from 'rc-slider';
import PoserManager from '../PoserManager';

import { Trans } from '@lingui/macro';

export default class BrightnessMenu extends Component {
  render() {
    // console.log('transform render');
    return (
      <div className="side-menu">

        <Button block><Trans>Brightness</Trans></Button>
        <ListGroup>
          <ListGroupItem>
              <Slider
                min={0.0}
                max={1.0}
                defaultValue={PoserManager.getBrightness()}
                step={0.01}
                onChange={PoserManager.setBrightness}
              />
          </ListGroupItem>
        </ListGroup>

        <Button block><Trans>GI strength</Trans></Button>
        <ListGroup>
          <ListGroupItem>
              <Slider
                min={0.0}
                max={1.0}
                defaultValue={PoserManager.getGIStrength()}
                step={0.01}
                onChange={PoserManager.setGIStrength}
              />
          </ListGroupItem>
        </ListGroup>
      </div>
    );
  }
}
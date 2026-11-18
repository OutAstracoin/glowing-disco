import React, { Component } from 'react';
import { Button, DropdownToggle } from 'reactstrap';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

function placeHolderOnclick() {
  console.log('placeholder button onclick function');
}

export default class ToolButton extends Component {
  render() {
    return (
      <Button onClick={this.props.onClick || placeHolderOnclick }>
        <FontAwesomeIcon size="2x" icon={this.props.icon} />
        <p className="label">{this.props.text}</p>
      </Button>
    );
  }
}

export class ToolDropdown extends Component {
  render() {
    // caret position?
    return (
      <DropdownToggle>
        <FontAwesomeIcon size="2x" icon={this.props.icon} />
        <p className="label">{this.props.text}</p>
      </DropdownToggle>
    );
  }
}
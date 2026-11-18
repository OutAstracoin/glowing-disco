import React, { Component } from 'react';
import { ButtonDropdown, DropdownMenu, DropdownItem } from 'reactstrap';

import { ToolDropdown } from './ToolButton';

import { faCube } from '@fortawesome/free-solid-svg-icons';
import PoserManager from '../PoserManager';

import { Trans } from '@lingui/macro';

export default class ViewSettings extends Component {
  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.state = {
      dropdownOpen: false
    };
  }

  toggle() {
    this.setState(prevState => ({
      dropdownOpen: !prevState.dropdownOpen
    }));
  }

  render() {
    return (
      <ButtonDropdown isOpen={this.state.dropdownOpen} toggle={this.toggle}>
         <ToolDropdown icon={faCube} text={<Trans>view</Trans>}></ToolDropdown>
        <DropdownMenu>
          <DropdownItem onClick={PoserManager.frameSelected}>{<Trans>Center</Trans>}</DropdownItem>
          <DropdownItem onClick={this.props.togglePerspectiveMenu}>{<Trans>Perspective</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(0)}}>{<Trans>Left</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(1)}}>{<Trans>Right</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(2)}}>{<Trans>Bottom</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(3)}}>{<Trans>Top</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(4)}}>{<Trans>Front</Trans>}</DropdownItem>
          <DropdownItem onClick={()=>{PoserManager.setView(5)}}>{<Trans>Back</Trans>}</DropdownItem>
        </DropdownMenu>
      </ButtonDropdown>
    );
  }
}
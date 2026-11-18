import React, { Component } from 'react';
import { ButtonDropdown, DropdownMenu, DropdownItem } from 'reactstrap';
import PoserManager from '../PoserManager';

import { ToolDropdown } from './ToolButton';
import { faCog } from '@fortawesome/free-solid-svg-icons';

import { Trans } from '@lingui/macro';

// Tool icon 

export default class ToggleSettings extends Component {
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
        <ToolDropdown icon={faCog} text={<Trans>toggle</Trans>}></ToolDropdown>
        <DropdownMenu>
          <DropdownItem onClick={PoserManager.toggleGrid}>{<Trans>Ground GuideLines</Trans>}</DropdownItem>
          <DropdownItem onClick={PoserManager.toggleSky}>{<Trans>Sky</Trans>}</DropdownItem>
          <DropdownItem onClick={PoserManager.toggleUnderwear}>{<Trans>Sportswear</Trans>}</DropdownItem>
          <DropdownItem divider />
          <DropdownItem header><Trans>Language</Trans></DropdownItem>
          <DropdownItem onClick={() => this.props.updateLanguage('en')}>English</DropdownItem>
          <DropdownItem onClick={() => this.props.updateLanguage('zh')}>中文</DropdownItem>
          <DropdownItem onClick={() => this.props.updateLanguage('ja')}>日本語</DropdownItem>
        </DropdownMenu>
      </ButtonDropdown>
    );
  }
}
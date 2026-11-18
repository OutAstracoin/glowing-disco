import React, { Component } from 'react';
import { Button, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';

import {isMobile} from 'react-device-detect';

import { Trans } from '@lingui/macro';

export class WarningModal extends Component {
  constructor(props) {
    super(props);

    var blocking = false;
    // detect webgl2 and webassemlby support
    (function() {
      var wasm_supported = (function() {
        try {
            if (typeof WebAssembly === "object"
                && typeof WebAssembly.instantiate === "function") {
                const module = new WebAssembly.Module(Uint8Array.of(0x0, 0x61, 0x73, 0x6d, 0x01, 0x00, 0x00, 0x00));
                if (module instanceof WebAssembly.Module)
                    return new WebAssembly.Instance(module) instanceof WebAssembly.Instance;
            }
        } catch (e) {
        }
        return false;
      })();

      var webgl2_supported = (function(){
        var canvas = document.createElement("canvas");
        var gl = canvas.getContext("webgl2");
        if (gl) {
          return true;
        }
        return false;
      })();

      blocking = !(wasm_supported && webgl2_supported);
      // blocking = true;

    })();

    this.blocking = blocking;


    this.state = {
      // modal: isMobile
      modal: true
    };

    this.toggle = this.toggle.bind(this);
  }

  toggle() {
    this.setState({
      modal: !this.state.modal
    });
  }

  render() {
    return (
      <div>

        {this.blocking && (<Modal isOpen={true}>
          <ModalBody>
            {/* <Trans id="mobile-web-warning"></Trans> */}
            <Trans id="browser-not-support"></Trans>
          </ModalBody>
        </Modal>)}

        {!this.blocking && isMobile && (<Modal isOpen={this.state.modal} toggle={this.toggle}>
          <ModalHeader toggle={this.toggle}><Trans id='try-mobile-app-title'></Trans></ModalHeader>
          <ModalBody>
            <Trans id="mobile-web-warning"></Trans>
          </ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={() => window.open("https://magicposer.com", "_blank")}><Trans id="download-page">Download Page</Trans></Button>
            <Button color="secondary" onClick={this.toggle}><Trans id="continue-webapp">Contine to use webapp</Trans></Button>
          </ModalFooter>
        </Modal>)}


      </div>
    );
  }
}
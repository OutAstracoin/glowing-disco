import React, { Component } from 'react';
import { Collapse, Button } from 'reactstrap';


import sphere from '../imgs/geometries/sphere_prop.png';
import cube from '../imgs/geometries/cube_prop.png';
import cone_prop from '../imgs/geometries/cone_prop.png';
import cylinder_prop from '../imgs/geometries/cylinder_prop.png';
import hex_prop from '../imgs/geometries/hex_prop.png';
import pyramid_prop from '../imgs/geometries/pyramid_prop.png';
import rectangle_prop from '../imgs/geometries/rectangle_prop.png';
import stairs_prop from '../imgs/geometries/stairs_prop.jpg';
import torus_prop from '../imgs/geometries/torus_prop.jpg';
import wall_prop from '../imgs/geometries/wall_prop.jpg';


import PoserManager from '../PoserManager';
import ItemCard from './ItemCard';
import ModelList from './ModelList';

import { Trans } from '@lingui/macro';


export default class AddObjectListMenu extends Component {
  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
    this.state = { unfold: -1 };
  }

  // toggle() {
  //   this.setState({ collapse: !this.state.collapse });
  // }
  toggle(id) {
    if (this.state.unfold === id) {
      id = -1;
    }

    this.setState({unfold: id});
  }

  render() {
    return (
      <div className="side-menu">
        {/* <ButtonGroup>
        <Button color="primary" onClick={this.toggle} zindex>Toggle</Button>
        
        </ButtonGroup>
        <Collapse isOpen={this.state.collapse}>
          <Card>
            <CardBody>
            Anim pariatur cliche reprehenderit,
             enim eiusmod high life accusamus terry richardson ad squid. Nihil
             anim keffiyeh helvetica, craft beer labore wes anderson cred
             nesciunt sapiente ea proident.
            </CardBody>
          </Card>
        </Collapse> */}

        <Button block onClick={() => {this.toggle(0)}}>{<Trans>Characters</Trans>}</Button>
        <Collapse isOpen={this.state.unfold === 0}>
          <ModelList onClickFunc={PoserManager.addModel} />
        </Collapse>


        <Button block onClick={() => {this.toggle(1)}}>{<Trans>Geometry</Trans>}</Button>
        <Collapse isOpen={this.state.unfold === 1}>
          {/* <ListGroup> */}
            {/* <ListGroupItem> */}
            <div className="list">
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Sphere</Trans>}
                src='sphere'
                img={sphere}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Cube</Trans>}
                src='cube_prop' 
                img={cube}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Cone</Trans>}
                src='cone_prop' 
                img={cone_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Cylinder</Trans>}
                src='cylinder_prop' 
                img={cylinder_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Hex</Trans>}
                src='hex_prop' 
                img={hex_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Pyramid</Trans>}
                src='pyramid_prop' 
                img={pyramid_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Rectangle</Trans>}
                src='rectangle_prop' 
                img={rectangle_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Stairs</Trans>}
                src='stairs_prop' 
                img={stairs_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Torus</Trans>}
                src='torus_prop' 
                img={torus_prop}>
              </ItemCard>
              <ItemCard 
                poserFunc={PoserManager.addProp} 
                name={<Trans>Wall</Trans>}
                src='wall_prop' 
                img={wall_prop}>
              </ItemCard>
              </div>
            {/* </ListGroupItem> */}
          {/* </ListGroup> */}
        </Collapse>

      </div>
    );
  }
}
import React, { Component } from 'react';
import { CardImg, CardBody, Card } from 'reactstrap';

export default class ItemCard extends Component {

  constructor(props) {
    super(props);
    this.click = this.click.bind(this);
  }

  click() {
    this.props.poserFunc(this.props.src);
  }

  render() {
    return (
      <Card className="text-center" onClick={this.click}>
        <CardImg top src={this.props.img} />
        <CardBody>{this.props.name}</CardBody>
      </Card>
    );
  }
}
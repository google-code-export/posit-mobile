<?php

/**
 * device form base class.
 *
 * @package    form
 * @subpackage device
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BasedeviceForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'              => new sfWidgetFormInputHidden(),
      'imei'            => new sfWidgetFormInput(),
      'name'            => new sfWidgetFormInput(),
      'auth_key'        => new sfWidgetFormInput(),
      'registered_date' => new sfWidgetFormDateTime(),
    ));

    $this->setValidators(array(
      'id'              => new sfValidatorDoctrineChoice(array('model' => 'device', 'column' => 'id', 'required' => false)),
      'imei'            => new sfValidatorString(array('max_length' => 128)),
      'name'            => new sfValidatorString(array('max_length' => 128, 'required' => false)),
      'auth_key'        => new sfValidatorString(array('max_length' => 128)),
      'registered_date' => new sfValidatorDateTime(),
    ));

    $this->validatorSchema->setPostValidator(
      new sfValidatorAnd(array(
        new sfValidatorDoctrineUnique(array('model' => 'device', 'column' => array('imei'))),
        new sfValidatorDoctrineUnique(array('model' => 'device', 'column' => array('auth_key'))),
      ))
    );

    $this->widgetSchema->setNameFormat('device[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'device';
  }

}

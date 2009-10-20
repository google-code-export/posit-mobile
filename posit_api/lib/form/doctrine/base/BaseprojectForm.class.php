<?php

/**
 * project form base class.
 *
 * @package    form
 * @subpackage project
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseprojectForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'           => new sfWidgetFormInputHidden(),
      'title'        => new sfWidgetFormInput(),
      'description'  => new sfWidgetFormInput(),
      'created_date' => new sfWidgetFormDateTime(),
      'updated_date' => new sfWidgetFormDateTime(),
    ));

    $this->setValidators(array(
      'id'           => new sfValidatorDoctrineChoice(array('model' => 'project', 'column' => 'id', 'required' => false)),
      'title'        => new sfValidatorString(array('max_length' => 128)),
      'description'  => new sfValidatorPass(array('required' => false)),
      'created_date' => new sfValidatorDateTime(),
      'updated_date' => new sfValidatorDateTime(),
    ));

    $this->widgetSchema->setNameFormat('project[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'project';
  }

}

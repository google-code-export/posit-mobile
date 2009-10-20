<?php

require_once(sfConfig::get('sf_lib_dir').'/filter/doctrine/BaseFormFilterDoctrine.class.php');

/**
 * device filter form base class.
 *
 * @package    filters
 * @subpackage device *
 * @version    SVN: $Id: sfDoctrineFormFilterGeneratedTemplate.php 11675 2008-09-19 15:21:38Z fabien $
 */
class BasedeviceFormFilter extends BaseFormFilterDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'imei'            => new sfWidgetFormFilterInput(),
      'name'            => new sfWidgetFormFilterInput(),
      'auth_key'        => new sfWidgetFormFilterInput(),
      'registered_date' => new sfWidgetFormFilterDate(array('from_date' => new sfWidgetFormDate(), 'to_date' => new sfWidgetFormDate(), 'with_empty' => false)),
    ));

    $this->setValidators(array(
      'imei'            => new sfValidatorPass(array('required' => false)),
      'name'            => new sfValidatorPass(array('required' => false)),
      'auth_key'        => new sfValidatorPass(array('required' => false)),
      'registered_date' => new sfValidatorDateRange(array('required' => false, 'from_date' => new sfValidatorDate(array('required' => false)), 'to_date' => new sfValidatorDate(array('required' => false)))),
    ));

    $this->widgetSchema->setNameFormat('device_filters[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'device';
  }

  public function getFields()
  {
    return array(
      'id'              => 'Number',
      'imei'            => 'Text',
      'name'            => 'Text',
      'auth_key'        => 'Text',
      'registered_date' => 'Date',
    );
  }
}
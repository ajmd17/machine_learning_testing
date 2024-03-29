type Value = string | number;
type JSONObject = { [key: string]: any };
enum OpType {
  CATEGORICAL = 'categorical',
  CONTINUOUS = 'continuous'
}

type DataType = 'string' | 'number';

abstract class DataField {
  constructor(public dataType: DataType) {}

  validateSelf(): void {
    if (['string', 'number'].indexOf(typeof this.dataType) === -1) {
      throw new Error('invalid data type');
    }
  }

  validateForValue(value: Value): void {
    if (typeof value !== this.dataType) {
      throw new Error(`Expected type '${this.dataType}', got '${typeof value}'`);
    }
  }
}

class ModelSchema<DataFieldType> {
  shape: { [key: string]: DataField };

  validateInputData(inputData: InputData) {
    let errors: string[] = [];
    
    // make sure input has all the same properties...
    for (let key in this.shape) {
      if (!Object.prototype.hasOwnProperty.call(this.shape, key)) {
        continue;
      }

      const schemaField = this.shape[key];

      if (!Object.prototype.hasOwnProperty.call(inputData.input, key)) {
        errors.push(`Input data does not contain field "${key}".`);
      }

      const inputValue = inputData.input[key];

      try {
        schemaField.validateForValue(inputValue)
      } catch (err) {
        errors.push(`Validation for field '${key}' failed. ${err.message}`);
      }
    }

    if (errors.length != 0) {
      throw new Error(errors.join('\n'));
    }
  }
}

abstract class Model<T extends DataField, DataType> {
  constructor(public schema: ModelSchema<T>, public data: DataType) {}

  abstract merge(other: Model<T, DataType>): Model<T, DataType>;
  abstract serialize(): JSONObject;
}


class InputData {
  constructor(public input: { [key: string]: Value }) {
  }

  validateAgainstSchema<T extends DataField>(schema: ModelSchema<T>) {
    schema.validateInputData(this);
  }
}


class PMMLDataField extends DataField {
  public possibleValues?: Value[];
  public opType: OpType;

  constructor(opts: { dataType: DataType, possibleValues?: Value[], opType: OpType }) {
    super(opts.dataType);
    this.opType = opts.opType;
    this.possibleValues = opts.possibleValues;
  }

  validateSelf() {
    super.validateSelf();

    if (this.opType === null || this.opType === undefined) {
      throw new Error('opType not defined');
    }
  }

  validateForValue(value: Value) {
    super.validateForValue(value);

    if (this.opType == OpType.CATEGORICAL) {
      if (this.possibleValues.indexOf(value) === -1) {
        throw new Error(`'${String(value)}' is not a valid value, expected one of: ${this.possibleValues.map(x => `'${x}'`).join(', ')}`);
      }
    }
  }
}

class PMMLModel extends Model<PMMLDataField, string> {
  merge(other: PMMLModel) {
    return this;
  }

  serialize(): JSONObject {
    return {
      schema: this.schema,
      data: this.data
    };
  }

  static deserialize(obj: JSONObject) {
    if (obj.schema == null) {
      throw new Error("'schema' field missing from serialized JSON object");
    }

    if (obj.data == null) {
      throw new Error("'data' field missing from serialized JSON object");
    }

    if (obj.schema.shape == null) {
      throw new Error("'schema.shape' field missing from serialized JSON object");
    }

    let modelSchema = new ModelSchema<PMMLDataField>();

    for (let key in obj.schema.shape) {
      let dataField = obj.schema.shape[key];

      if (dataField.opType == null) {
        throw new Error('opType not specified for data field');
      }

      if (dataField.dataType == null) {
        throw new Error('dataType not specified for data field');
      }

      let dataFieldObj = new PMMLDataField({
        dataType: dataField.dataType,
        possibleValues: dataField.possibleValues,
        opType: dataField.opType
      });

      dataFieldObj.validateSelf();

      modelSchema.shape[key] = dataFieldObj;
    }

    return new PMMLModel(modelSchema, obj.data);
  }
}


class ONNXDataField extends DataField {
  constructor(opts: { dataType: DataType }) {
    super(opts.dataType);
  }

  /** TODO */
}

class ONNXModel extends Model<ONNXDataField, Uint8Array> {
  merge(other: ONNXModel) {
    return this;
  }

  serialize(): JSONObject {
    return {
      schema: this.schema,
      data: this.data
    };
  }
}


function main() {
  PMMLModel.deserialize({
    schema: {
      shape: {
        
      }
    },
    data: `
    <PMML xmlns="http://www.dmg.org/PMML-4_1" version="4.1">
    <Header copyright="KNIME">
    <Application name="KNIME" version="2.8.0"/>
    </Header>
    <DataDictionary numberOfFields="10">
    <DataField dataType="integer" name="Age" optype="continuous">
    <Interval closure="closedClosed" leftMargin="17.0" rightMargin="90.0"/>
    </DataField>
    <DataField dataType="string" name="Employment" optype="categorical">
    <Value value="Private"/>
    <Value value="Consultant"/>
    <Value value="SelfEmp"/>
    <Value value="PSLocal"/>
    <Value value="PSState"/>
    <Value value="PSFederal"/>
    <Value value="Unemployed"/>
    <Value value="NA"/>
    <Value value="Volunteer"/>
    </DataField>
    <DataField dataType="string" name="Education" optype="categorical">
    <Value value="College"/>
    <Value value="Associate"/>
    <Value value="HSgrad"/>
    <Value value="Bachelor"/>
    <Value value="Yr12"/>
    <Value value="Vocational"/>
    <Value value="Master"/>
    <Value value="Yr11"/>
    <Value value="Yr10"/>
    <Value value="Doctorate"/>
    <Value value="Yr9"/>
    <Value value="Yr5t6"/>
    <Value value="Professional"/>
    <Value value="Yr7t8"/>
    <Value value="Preschool"/>
    <Value value="Yr1t4"/>
    </DataField>
    <DataField dataType="string" name="Marital" optype="categorical">
    <Value value="Unmarried"/>
    <Value value="Absent"/>
    <Value value="Divorced"/>
    <Value value="Married"/>
    <Value value="Widowed"/>
    <Value value="Married-spouse-absent"/>
    </DataField>
    <DataField dataType="string" name="Occupation" optype="categorical">
    <Value value="Service"/>
    <Value value="Transport"/>
    <Value value="Clerical"/>
    <Value value="Repair"/>
    <Value value="Executive"/>
    <Value value="Machinist"/>
    <Value value="Sales"/>
    <Value value="Professional"/>
    <Value value="Support"/>
    <Value value="Cleaner"/>
    <Value value="Farming"/>
    <Value value="NA"/>
    <Value value="Protective"/>
    <Value value="Home"/>
    <Value value="Military"/>
    </DataField>
    <DataField dataType="double" name="Income" optype="continuous">
    <Interval closure="closedClosed" leftMargin="609.72" rightMargin="481259.5"/>
    </DataField>
    <DataField dataType="string" name="Gender" optype="categorical">
    <Value value="Female"/>
    <Value value="Male"/>
    </DataField>
    <DataField dataType="double" name="Deductions" optype="continuous">
    <Interval closure="closedClosed" leftMargin="0.0" rightMargin="2904.0"/>
    </DataField>
    <DataField dataType="integer" name="Hours" optype="continuous">
    <Interval closure="closedClosed" leftMargin="1.0" rightMargin="99.0"/>
    </DataField>
    <DataField dataType="integer" name="TARGET_Adjusted" optype="continuous">
    <Interval closure="closedClosed" leftMargin="0.0" rightMargin="1.0"/>
    </DataField>
    </DataDictionary>
    <TransformationDictionary/>
    <ClusteringModel modelName="k-means" functionName="clustering" modelClass="centerBased" numberOfClusters="4">
    <MiningSchema>
    <MiningField name="Age" invalidValueTreatment="asIs"/>
    <MiningField name="Income" invalidValueTreatment="asIs"/>
    <MiningField name="Deductions" invalidValueTreatment="asIs"/>
    <MiningField name="Hours" invalidValueTreatment="asIs"/>
    <MiningField name="Employment" invalidValueTreatment="asIs"/>
    <MiningField name="Education" invalidValueTreatment="asIs"/>
    <MiningField name="Marital" invalidValueTreatment="asIs"/>
    <MiningField name="Occupation" invalidValueTreatment="asIs"/>
    <MiningField name="Gender" invalidValueTreatment="asIs"/>
    </MiningSchema>
    <LocalTransformations>
    <DerivedField dataType="integer" name="Private_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="Private"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Consultant_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="Consultant"/>
    </DerivedField>
    <DerivedField dataType="integer" name="SelfEmp_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="SelfEmp"/>
    </DerivedField>
    <DerivedField dataType="integer" name="PSLocal_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="PSLocal"/>
    </DerivedField>
    <DerivedField dataType="integer" name="PSState_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="PSState"/>
    </DerivedField>
    <DerivedField dataType="integer" name="PSFederal_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="PSFederal"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Unemployed_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="Unemployed"/>
    </DerivedField>
    <DerivedField dataType="integer" name="NA_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="NA"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Volunteer_Employment" optype="ordinal">
    <NormDiscrete field="Employment" mapMissingTo="0.0" value="Volunteer"/>
    </DerivedField>
    <DerivedField dataType="integer" name="College_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="College"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Associate_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Associate"/>
    </DerivedField>
    <DerivedField dataType="integer" name="HSgrad_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="HSgrad"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Bachelor_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Bachelor"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr12_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr12"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Vocational_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Vocational"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Master_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Master"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr11_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr11"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr10_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr10"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Doctorate_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Doctorate"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr9_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr9"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr5t6_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr5t6"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Professional_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Professional"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr7t8_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr7t8"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Preschool_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Preschool"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Yr1t4_Education" optype="ordinal">
    <NormDiscrete field="Education" mapMissingTo="0.0" value="Yr1t4"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Unmarried_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Unmarried"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Absent_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Absent"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Divorced_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Divorced"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Married_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Married"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Widowed_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Widowed"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Married-spouse-absent_Marital" optype="ordinal">
    <NormDiscrete field="Marital" mapMissingTo="0.0" value="Married-spouse-absent"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Service_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Service"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Transport_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Transport"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Clerical_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Clerical"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Repair_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Repair"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Executive_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Executive"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Machinist_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Machinist"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Sales_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Sales"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Professional_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Professional"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Support_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Support"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Cleaner_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Cleaner"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Farming_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Farming"/>
    </DerivedField>
    <DerivedField dataType="integer" name="NA_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="NA"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Protective_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Protective"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Home_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Home"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Military_Occupation" optype="ordinal">
    <NormDiscrete field="Occupation" mapMissingTo="0.0" value="Military"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Female_Gender" optype="ordinal">
    <NormDiscrete field="Gender" mapMissingTo="0.0" value="Female"/>
    </DerivedField>
    <DerivedField dataType="integer" name="Male_Gender" optype="ordinal">
    <NormDiscrete field="Gender" mapMissingTo="0.0" value="Male"/>
    </DerivedField>
    </LocalTransformations>
    <ComparisonMeasure kind="distance">
    <squaredEuclidean/>
    </ComparisonMeasure>
    <ClusteringField field="Age" compareFunction="absDiff"/>
    <ClusteringField field="Income" compareFunction="absDiff"/>
    <ClusteringField field="Deductions" compareFunction="absDiff"/>
    <ClusteringField field="Hours" compareFunction="absDiff"/>
    <ClusteringField field="Private_Employment" compareFunction="absDiff"/>
    <ClusteringField field="Consultant_Employment" compareFunction="absDiff"/>
    <ClusteringField field="SelfEmp_Employment" compareFunction="absDiff"/>
    <ClusteringField field="PSLocal_Employment" compareFunction="absDiff"/>
    <ClusteringField field="PSState_Employment" compareFunction="absDiff"/>
    <ClusteringField field="PSFederal_Employment" compareFunction="absDiff"/>
    <ClusteringField field="Unemployed_Employment" compareFunction="absDiff"/>
    <ClusteringField field="NA_Employment" compareFunction="absDiff"/>
    <ClusteringField field="Volunteer_Employment" compareFunction="absDiff"/>
    <ClusteringField field="College_Education" compareFunction="absDiff"/>
    <ClusteringField field="Associate_Education" compareFunction="absDiff"/>
    <ClusteringField field="HSgrad_Education" compareFunction="absDiff"/>
    <ClusteringField field="Bachelor_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr12_Education" compareFunction="absDiff"/>
    <ClusteringField field="Vocational_Education" compareFunction="absDiff"/>
    <ClusteringField field="Master_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr11_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr10_Education" compareFunction="absDiff"/>
    <ClusteringField field="Doctorate_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr9_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr5t6_Education" compareFunction="absDiff"/>
    <ClusteringField field="Professional_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr7t8_Education" compareFunction="absDiff"/>
    <ClusteringField field="Preschool_Education" compareFunction="absDiff"/>
    <ClusteringField field="Yr1t4_Education" compareFunction="absDiff"/>
    <ClusteringField field="Unmarried_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Absent_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Divorced_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Married_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Widowed_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Married-spouse-absent_Marital" compareFunction="absDiff"/>
    <ClusteringField field="Service_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Transport_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Clerical_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Repair_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Executive_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Machinist_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Sales_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Professional_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Support_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Cleaner_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Farming_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="NA_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Protective_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Home_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Military_Occupation" compareFunction="absDiff"/>
    <ClusteringField field="Female_Gender" compareFunction="absDiff"/>
    <ClusteringField field="Male_Gender" compareFunction="absDiff"/>
    <Cluster name="cluster_0" size="289">
    <Array n="52" type="real">
    32.11072664359862 172262.55875432526 19.397923875432525 35.98615916955017 0.7889273356401384 0.04152249134948097 0.010380622837370242 0.04844290657439446 0.04152249134948097 0.020761245674740483 0.0034602076124567475 0.04498269896193772 0.0 0.2768166089965398 0.031141868512110725 0.38408304498269896 0.09342560553633218 0.01730103806228374 0.03460207612456748 0.020761245674740483 0.06920415224913495 0.02422145328719723 0.006920415224913495 0.010380622837370242 0.010380622837370242 0.0 0.020761245674740483 0.0 0.0 0.06228373702422145 0.5778546712802768 0.17301038062283736 0.1314878892733564 0.03806228373702422 0.01730103806228374 0.16262975778546712 0.031141868512110725 0.20069204152249134 0.04498269896193772 0.09342560553633218 0.08650519031141868 0.11072664359861592 0.0726643598615917 0.02768166089965398 0.06920415224913495 0.04152249134948097 0.04844290657439446 0.006920415224913495 0.0034602076124567475 0.0 0.5743944636678201 0.42560553633217996
    </Array>
    </Cluster>
    <Cluster name="cluster_1" size="536">
    <Array n="52" type="real">
    35.235074626865675 92642.61884328352 51.079601990049774 38.78358208955224 0.7220149253731343 0.0708955223880597 0.020522388059701493 0.05970149253731343 0.03544776119402985 0.03544776119402985 0.0 0.055970149253731345 0.0 0.23694029850746268 0.03544776119402985 0.31343283582089554 0.16791044776119404 0.011194029850746268 0.041044776119402986 0.03544776119402985 0.055970149253731345 0.03544776119402985 0.011194029850746268 0.011194029850746268 0.009328358208955223 0.014925373134328358 0.014925373134328358 0.005597014925373134 0.0 0.05783582089552239 0.5746268656716418 0.22201492537313433 0.08582089552238806 0.04664179104477612 0.013059701492537313 0.1417910447761194 0.03731343283582089 0.1287313432835821 0.08208955223880597 0.11753731343283583 0.05223880597014925 0.10261194029850747 0.13619402985074627 0.022388059701492536 0.05223880597014925 0.03171641791044776 0.055970149253731345 0.029850746268656716 0.007462686567164179 0.0018656716417910447 0.4533582089552239 0.5466417910447762
    </Array>
    </Cluster>
    <Cluster name="cluster_2" size="118">
    <Array n="52" type="real">
    37.0 270908.5802542373 45.07627118644068 34.74576271186441 0.788135593220339 0.059322033898305086 0.025423728813559324 0.0423728813559322 0.01694915254237288 0.00847457627118644 0.0 0.059322033898305086 0.0 0.23728813559322035 0.059322033898305086 0.3728813559322034 0.1271186440677966 0.00847457627118644 0.0423728813559322 0.025423728813559324 0.025423728813559324 0.01694915254237288 0.0 0.025423728813559324 0.00847457627118644 0.01694915254237288 0.03389830508474576 0.0 0.0 0.05084745762711865 0.3220338983050847 0.2796610169491525 0.3050847457627119 0.03389830508474576 0.00847457627118644 0.13559322033898305 0.00847457627118644 0.211864406779661 0.07627118644067797 0.06779661016949153 0.0847457627118644 0.11864406779661017 0.1440677966101695 0.01694915254237288 0.059322033898305086 0.00847457627118644 0.059322033898305086 0.00847457627118644 0.0 0.0 0.7203389830508474 0.2796610169491525
    </Array>
    </Cluster>
    <Cluster name="cluster_3" size="1057">
    <Array n="52" type="real">
    42.30085146641438 35921.839583727546 91.61116367076633 42.44181646168401 0.6650898770104068 0.08609271523178808 0.0586565752128666 0.06433301797540208 0.036896877956480605 0.04068117313150426 0.0 0.04730368968779565 9.46073793755913E-4 0.195837275307474 0.03216650898770104 0.31882686849574265 0.20151371807000945 0.004730368968779565 0.046357615894039736 0.07000946073793755 0.019867549668874173 0.02838221381267739 0.017975402081362345 0.014191106906338695 0.013245033112582781 0.013245033112582781 0.015137180700094607 0.002838221381267739 0.005676442762535478 0.011352885525070956 0.14758751182592242 0.06054872280037843 0.7540208136234626 0.017975402081362345 0.008514664143803218 0.06717123935666983 0.0728476821192053 0.07568590350047304 0.15042573320719016 0.18070009460737937 0.07190160832544938 0.09933774834437085 0.12866603595080417 0.02554399243140965 0.03405865657521287 0.026490066225165563 0.04730368968779565 0.019867549668874173 0.0 0.0 0.130558183538316 0.8694418164616841
    </Array>
    </Cluster>
    </ClusteringModel>
    </PMML>
    `
  })
}

main();
package functions

import org.apache.spark.sql.types.{DataType, NumericType, StructField, StructType}

/**
  * Created by yhao on 2017/3/14.
  */
object MySchemaUtils extends Serializable {
  /**
    * Check whether the given schema contains a column of the required data type.
    * @param colName  column name
    * @param dataType  required column data type
    */
  def checkColumnType(
                       schema: StructType,
                       colName: String,
                       dataType: DataType,
                       msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.equals(dataType),
      s"Column $colName must be of type $dataType but was actually $actualDataType.$message")
  }

  /**
    * Check whether the given schema contains a column of one of the require data types.
    * @param colName  column name
    * @param dataTypes  required column data types
    */
  def checkColumnTypes(
                        schema: StructType,
                        colName: String,
                        dataTypes: Seq[DataType],
                        msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(dataTypes.exists(actualDataType.equals),
      s"Column $colName must be of type equal to one of the following types: " +
        s"${dataTypes.mkString("[", ", ", "]")} but was actually of type $actualDataType.$message")
  }

  /**
    * Check whether the given schema contains a column of the numeric data type.
    * @param colName  column name
    */
  def checkNumericType(
                        schema: StructType,
                        colName: String,
                        msg: String = ""): Unit = {
    val actualDataType = schema(colName).dataType
    val message = if (msg != null && msg.trim.length > 0) " " + msg else ""
    require(actualDataType.isInstanceOf[NumericType], s"Column $colName must be of type " +
      s"NumericType but was actually of type $actualDataType.$message")
  }

  /**
    * Appends a new column to the input schema. This fails if the given output column already exists.
    * @param schema input schema
    * @param colName new column name. If this column name is an empty string "", this method returns
    *                the input schema unchanged. This allows users to disable output columns.
    * @param dataType new column data type
    * @return new schema with the input column appended
    */
  def appendColumn(
                    schema: StructType,
                    colName: String,
                    dataType: DataType,
                    nullable: Boolean = false): StructType = {
    if (colName.isEmpty) return schema
    appendColumn(schema, StructField(colName, dataType, nullable))
  }

  /**
    * Appends a new column to the input schema. This fails if the given output column already exists.
    * @param schema input schema
    * @param col New column schema
    * @return new schema with the input column appended
    */
  def appendColumn(schema: StructType, col: StructField): StructType = {
    require(!schema.fieldNames.contains(col.name), s"Column ${col.name} already exists.")
    StructType(schema.fields :+ col)
  }
}

package io.kroom.api.util

case class DataError(
                      field: String,
                      errors: List[String],
                    )

case class DataPayload[T](
                           data: Option[T],
                           errors: List[DataError]
                         )

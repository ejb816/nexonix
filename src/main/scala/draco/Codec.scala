package org.nexonix.format.json

import io.circe.{Decoder, Encoder}

import scala.reflect.ClassTag

trait Codec[T] {
  val encoder: Encoder[T]
  val decoder: Decoder[T]
}

object Codec {
  def apply[T](
    _encoder: Encoder[T],
    _decoder: Decoder[T]
  ): Codec[T] = new Codec[T] {
    override val encoder: Encoder[T] = _encoder
    override val decoder: Decoder[T] = _decoder
  }

  def sub[P, T <: P](
    parentEncoder: Encoder[P],
    parentDecoder: Decoder[P]
  )(implicit ct: ClassTag[T]): Codec[T] =
    Codec(
      parentEncoder.contramap[T](identity),
      parentDecoder.emap {
        case t: T => Right(t)
        case other => Left(s"Expected ${ct.runtimeClass.getSimpleName}, got ${other.getClass.getSimpleName}")
      }
    )
}

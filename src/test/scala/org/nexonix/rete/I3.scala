package org.nexonix.rete

trait I3 extends draco.Fact[(Int,Int,Int)] {}

object I3 {
  def apply (_i1: Int, _i2: Int, _i3: Int) : I3 = new I3 {}
}
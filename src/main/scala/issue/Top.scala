/*
 * SpinalHDL
 * Copyright (c) Dolu, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package issue

import spinal.core._

class Top(initHexfile:String) extends Component {
  val io = new Bundle {
  }
  val rtl = new RTLCore()
  val ram = new Memory(32,512,initHexfile)

  // bus interconnect
  rtl.io.sb <> ram.io.sb

  // memory mapping
  val addressMapping = new Area{
    val addr = rtl.io.sb.SBaddress
    ram.io.sel := False
    when(addr < 0x00000200){
      ram.io.sel := True
    }
  }
}

//Generate the Memory's Verilog
object TopVerilog {
  def main(args: Array[String]) {
    SpinalConfig(targetDirectory = "rtl").generateVerilog(new Top("dumps/addsave.hex")).printPruned()
  }
}
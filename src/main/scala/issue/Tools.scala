package issue

import spinal.core._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object Tools {

  //From https://github.com/SpinalHDL/SpinalHDL/issues/160#issuecomment-445445201
  //Until there is a spinalhdl built-in method for binding memory content like readmemh in verilog
  def readmemh(path: String): Array[Bits] = {
    val buffer = new ArrayBuffer[Bits]
    for (line <- Source.fromFile(path).getLines) {
      val tokens: Array[String] = line.split("(//)").map(_.trim)
      if (tokens.length > 0 && tokens(0) != "") {
        val i = BigInt(tokens(0), 16)
        buffer.append(B(i))
      }
    }
    println(buffer.toString())
    buffer.toArray
  }

  def readBytesFromTxt(path: String): Array[Bits] = {
    val buffer = new ArrayBuffer[Bits]
    for(line <- Source.fromFile(path).getLines){
      val bytes : Array[String] = line.split(" ").map(_.trim)
      val parsedWord : BigInt = 0
      for(byte <- bytes){
        buffer.append(B(Integer.parseUnsignedInt(byte,32)))
      }
    }
    println(buffer.toString())
    buffer.toArray
  }
}
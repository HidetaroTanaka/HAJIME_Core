`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 11/28/2023 07:11:11 PM
// Design Name: 
// Module Name: sim
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module sim();
  wire [31:0] tohost;
  reg CLK, RST;

  top top(
    .CLK(CLK),
    .RST(RST),
    .tohost(tohost)
  );

  initial begin
    RST = 1;
    CLK = 0;

    #256 RST = 0;
    #256 RST = 1;
    #1048576 $finish;
  end

  always begin
    #2 CLK = ~CLK;
  end

endmodule

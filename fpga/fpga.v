`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company:
// Engineer:
//
// Create Date: 12/01/2023 04:05:10 PM
// Design Name:
// Module Name: fpga
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


module fpga(
    input clk_in1_0,
    input RST_0,
    output [6:0] SEG_0,
	output [7:0] AN_0
    );
    wire [31:0] check;
    wire CLK_top;

    design_1_wrapper clk_wiz(
        .clk_in1_0(clk_in1_0),
        .clk_out1_0(CLK_top)
    );

    top top(
        .CLK(CLK_top),
        .RST(RST_0),
        .tohost(check)
    );
    SEG_PUT seg_put(
        .CLK(clk_in1_0),
        .reset(RST_0),
        .check(check),
        .SEG(SEG_0),
        .AN(AN_0)
    );
endmodule

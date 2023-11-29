`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: Nakajo lab.
// Engineer: Hironari Yoshiuchi
// 
// Create Date:    16:56:03 05/12/2014 
// Design Name: 
// Module Name:    SEG_PUT 
// Project Name: 
// Target Devices: Nexys4
// Tool versions: 
// Description: 
//
// Dependencies: 
//
// Revision: 
// Revision 0.01 - File Created
// Additional Comments: 
//
//////////////////////////////////////////////////////////////////////////////////
module SEG_PUT(CLK, reset, check, SEG, AN
    );
	 input CLK, reset;
	 input [31:0] check;
	 output [6:0] SEG;
	 output [7:0] AN;
	 
	 reg [2:0] SEG_state;

	 wire SEG_CLK;

	 SEG_CLK_GEN SEG_CLK_GEN(.CLK(CLK), .reset(reset), .SEG_CLK(SEG_CLK));

	 assign AN = (SEG_state == 0) ? 8'b11111110:
					 (SEG_state == 1) ? 8'b11111101:
					 (SEG_state == 2) ? 8'b11111011:
					 (SEG_state == 3) ? 8'b11110111:
					 (SEG_state == 4) ? 8'b11101111:
					 (SEG_state == 5) ? 8'b11011111:
					 (SEG_state == 6) ? 8'b10111111:
					 (SEG_state == 7) ? 8'b01111111:
					 /*�����Ȓl*/			  8'b11111111;

	 assign SEG = (SEG_state == 0) ? hex7seg(check[3:0]):
					  (SEG_state == 1) ? hex7seg(check[7:4]):
					  (SEG_state == 2) ? hex7seg(check[11:8]):
					  (SEG_state == 3) ? hex7seg(check[15:12]):
					  (SEG_state == 4) ? hex7seg(check[19:16]):
					  (SEG_state == 5) ? hex7seg(check[23:20]):
					  (SEG_state == 6) ? hex7seg(check[27:24]):
					  (SEG_state == 7) ? hex7seg(check[31:28]):
					  /*�����Ȓl*/		   7'b0000000;


	 always@(posedge SEG_CLK or negedge reset) begin
		if(!reset) begin
			SEG_state <= 0;
		end else begin
			if(SEG_state == 7)	SEG_state <= 0;
			else	SEG_state <= SEG_state + 1;
		end
	 end


	 function [6:0] hex7seg;
		input [3:0] hex;
		begin
			case (hex)
				 0: hex7seg = 7'b1000000;
				 1: hex7seg = 7'b1111001;
				 2: hex7seg = 7'b0100100;
				 3: hex7seg = 7'b0110000;
				 4: hex7seg = 7'b0011001;
				 5: hex7seg = 7'b0010010;
				 6: hex7seg = 7'b0000010;
				 7: hex7seg = 7'b1111000;
				 8: hex7seg = 7'b0000000;
				 9: hex7seg = 7'b0010000;
				10: hex7seg = 7'b0001000;
				11: hex7seg = 7'b0000011;
				12: hex7seg = 7'b1000110;
				13: hex7seg = 7'b0100001;
				14: hex7seg = 7'b0000110;
				15: hex7seg = 7'b0001110;
			endcase
		end
	endfunction
endmodule

module SEG_CLK_GEN(CLK, reset, SEG_CLK
	 );
	 input CLK, reset;
	 output reg SEG_CLK;
	 
	 reg [16:0] cnt;
	 
	 always@(posedge CLK or negedge reset) begin
		if(!reset) begin
			cnt <= 0;
			SEG_CLK <= 0;
		end else if(cnt == 100000) begin
			cnt <= 0;
			SEG_CLK <= 1;
		end else begin
			cnt <= cnt + 1;
			SEG_CLK <= 0;
		end
	 end
endmodule
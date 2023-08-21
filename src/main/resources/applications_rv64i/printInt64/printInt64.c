extern void printstr(char* str);
extern void int64ToHex(long num, char* str);

int main(int argc, char** argv) {
    char string[19];
    int64ToHex(0x1145141919810, string);
    printstr(string);
    printstr("\n");
    return 0;
}
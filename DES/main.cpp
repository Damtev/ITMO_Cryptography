#include <iostream>
#include <fstream>
#include <bitset>
#include <cstring>
#include <numeric>
#include <vector>
#include <algorithm>
#include "des.h"

static const std::string usage = "Usage: des <input> <output> <option=[1,2]> [key]";

void block_from_string(std::bitset<64> &block, const char *str) {
    for (size_t i = 0; i < block.size(); ++i) {
        block[i] = (*((unsigned char *) (str) + i / 8) & (1 << (7 - i % 8))) != 0;
    }
}

void string_from_block(char *str, const std::bitset<64> &block) {
    memset(str, 0, 8);
    for (size_t i = 0; i < block.size(); ++i) {
        if (block[i]) {
            *((unsigned char *) (str) + i / 8) |= (1 << (7 - i % 8));
        }
    }
}


auto generate_key() {
    auto *key = (unsigned char *) malloc(8 * sizeof(char));
    int i;
    for (i = 0; i < 8; i++) {
        key[i] = rand() % 255;
    }
    return key;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        std::cout << usage << std::endl;
        return 0;
    }
    auto in_name = argv[1];
    auto out_name = argv[2];
    auto option = argv[3];

    std::ifstream in(in_name);
    std::ofstream out(out_name);
    std::bitset<64> key;
    block_from_string(key, argc > 4 ? argv[4] : reinterpret_cast<const char *>(generate_key()));
    char buffer[8];
    Des des;
    while (!in.eof()) {
        memset(buffer, 0, 8);
        in.read(buffer, 8);
        auto sum = std::accumulate(buffer, buffer + 8, 0);
        if (sum == 0) {
            break;
        }
        std::bitset<64> block;
        block_from_string(block, buffer);
        des.crypt(block, key, stoul(option));
        string_from_block(buffer, block);
        out.write(buffer, 8);
    }

    return 0;
}